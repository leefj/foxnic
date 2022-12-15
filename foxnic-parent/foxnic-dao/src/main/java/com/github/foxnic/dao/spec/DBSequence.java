package com.github.foxnic.dao.spec;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.busi.id.SequenceType;
import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.dao.data.Rcd;
import com.github.foxnic.dao.procedure.StoredProcedure;
import com.github.foxnic.sql.expr.Insert;

import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBSequence {

    private static final Set<String> SEQUENCE_IDS =new HashSet<>();
    private static final ConcurrentHashMap<String,Integer> SEQUENCE_FETCH_SIZE =new ConcurrentHashMap<String,Integer>();
    private static final ConcurrentHashMap<String,SequenceType> SEQUENCE_TYPES =new ConcurrentHashMap<String,SequenceType>();
    private static final ConcurrentHashMap<String,String> DATE_TAGS =new ConcurrentHashMap<String,String>();
    private static final ConcurrentHashMap<String,ConcurrentLinkedQueue<String>> SEQUENCES_POOL =new ConcurrentHashMap<String,ConcurrentLinkedQueue<String>>();


    private DAO dao;
    private String id;
    private String table;
    private Object tenantId;
    private String key;
    private String procedureName;

    public DBSequence(DAO dao, String id,String table,String procedureName) {
        this.dao=dao;
        this.id=id.trim().toLowerCase();
        this.table=table;
        this.procedureName=procedureName;
        if(dao.getDBTreaty()!=null) {
            this.tenantId=dao.getDBTreaty().getActivedTenantId();
        }
        if(StringUtil.isBlank(this.tenantId)) {
            this.tenantId = "defaults";
        }
        this.key=dao.getDbIdentity()+"."+tenantId+"."+id;
        SequenceType sequenceType= SEQUENCE_TYPES.get(this.key);

        Integer fetchSize= SEQUENCE_FETCH_SIZE.get(key);

        if(fetchSize==null || sequenceType==null) {
            init();
        }


    }

    private void init() {
        Rcd r=dao.queryRecord("select fetch_size,type from "+table+" where id=? and tenant_id=?",id,this.tenantId);
        if(r!=null) {
            Integer fetchSize = r.getInteger("fetch_size");
            SequenceType sequenceType = r.getEnum("type", SequenceType.class, null, "name");
            if (fetchSize == null) fetchSize = 4;
            SEQUENCE_FETCH_SIZE.put(key, fetchSize);
            SEQUENCE_TYPES.put(key, sequenceType);
        }
    }

    /**
     * 创建一个序列
     *
     * @param type 序列类型
     * @param len  序列长度
     * @return 是否创建成功
     */
    public boolean create(SequenceType type, int len) {
        int fetchSize=4;
        boolean suc=this.create(type,len,fetchSize);
        if (suc) {
            this.setFetchSize(fetchSize);
        }
        return suc;
    }

    /**
     * 创建一个序列
     *
     * @param type 序列类型
     * @param len  序列长度
     * @param fetchSize  取数个数
     * @return 是否创建成功
     */

        public boolean create(SequenceType type, int len,int fetchSize) {
        // 如果已经存在，就不需要创建了
        if (this.exists()) {
            return true;
        }
        try {
            Insert ins = dao.insert(table).set("PK", IDGenerator.getSnowflakeIdString()).set("ID", id).set("TYPE", type.name()).set("VALUE", 0).set("FETCH_SIZE",fetchSize)
                    .set("LENGTH", len).set("TENANT_ID",tenantId);
            int i=dao.execute(ins);
            this.setFetchSize(fetchSize);
            if (i == 1) {
                init();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (this.exists()) {
                return true;
            } else {
                Logger.exception("create sequence",e);
            }
            return false;
        }
    }


    private boolean createTenantCopy() {
        Rcd r=dao.queryRecord("select * from "+table+" where ID=?",id);
        if(r==null) return false;
        //
        SequenceType type= SequenceType.valueOf(r.getString("type"));
        boolean suc=this.create(type,r.getInteger("length"),r.getInteger("fetch_size"));
        return suc;
    }

    public boolean existInTable() {
        Integer count=dao.queryInteger("select count(1) from "+table+" where ID=? and tenant_id=?",id,tenantId);
        return count>0;
    }


    /**
     * 判断是否已经存在
     * */
    public boolean exists() {
        if (SEQUENCE_IDS.contains(key)) {
            return true;
        }
        boolean ex=existInTable();
        if (ex) {
            SEQUENCE_IDS.add(key);
        }
        return ex;
    }

    public void setFetchSize(int size) {
        SEQUENCE_FETCH_SIZE.put(key, size);
        dao.execute("update "+table+" set fetch_size=? where id=? and tenant_id=?",size,id,this.tenantId);
    }

    public Integer getFetchSize() {
        Integer size= SEQUENCE_FETCH_SIZE.get(key);
        if(size==null) {
            return 4;
        }
        return size.intValue()+1;
    }

    /**
     * 得到序列的下一个值
     *
     * @return 序列值
     */
    public String next() {
        ConcurrentLinkedQueue<String> queue= SEQUENCES_POOL.get(key);
        if(queue==null) {
            queue = new ConcurrentLinkedQueue<String>();
            SEQUENCES_POOL.put(key,queue);
        }
        String val=null;
        String dateTag=null;
        SequenceType sequenceType= SEQUENCE_TYPES.get(this.key);
        if(sequenceType.dateTagFormat()!=null) {
            dateTag=DateUtil.getCurrTime(sequenceType.dateTagFormat());
        }
        String currDateTag= DATE_TAGS.get(this.key);
        try {
            if(currDateTag!=null && dateTag!=null) {
                if(currDateTag.equals(dateTag)) {
                    val = queue.remove();
                } else {
                    queue.clear();
                }
            } else {
                val = queue.remove();
            }
        } catch (NoSuchElementException e) {
            val=null;
        }
        if(val!=null) {
            return val;
        }
        fetchSequenceValues(queue);
        return next();

    }

    /**
     * 得到序列的下一组值
     */
    private void fetchSequenceValues(ConcurrentLinkedQueue<String> quene) {
        int size=getFetchSize();
        if(!exists()) {
            Rcd r=dao.queryRecord("select * from "+table+" where id=? and tenant_id=?",this.id,this.tenantId);
            if(r==null) {
                if(createTenantCopy()) {
                    SEQUENCE_IDS.remove(key);
                    fetchSequenceValues(quene);
                    return;
                } else {
                    throw new RuntimeException("序列 " + this.id + " 未预先定义");
                }
            }
            SequenceType type=SequenceType.valueOf(r.getString("type"));
            this.create(type,r.getInteger("length"));
        }
        try {
            StoredProcedure p = dao.getStoredProcedure(procedureName);
            p.declareParameter("id", Types.VARCHAR);
            p.declareParameter("tid", Types.VARCHAR);
            p.declareParameter("num", Types.INTEGER);
            p.declareOutParameter("sval", Types.VARCHAR);
            Map<String, Object> ret = p.execute(MapUtil.asStringKeyMap("id", id,"tid",tenantId,"num",size));
            String vals=ret.get("sval") + "";
            SequenceType sequenceType= SEQUENCE_TYPES.get(this.key);
            if(sequenceType.dateTagFormat()!=null) {
                DATE_TAGS.put(this.key, DateUtil.getCurrTime(sequenceType.dateTagFormat()));
            }
            // 如果没有取到序列值
            if(StringUtil.isEmpty(vals)) {
                if(!existInTable()){
                    SEQUENCE_IDS.remove(key);
                    createTenantCopy();
                    fetchSequenceValues(quene);
                    return;
                } else {
                    throw new RuntimeException("获取序列失败");
                }
            }

            String[] valArr=vals.split(",");
            for (String val : valArr) {
                if(val==null || val.length()==0) {
                    continue;
                }
                quene.add(val);
            }
        } catch (Exception e) {
            Logger.exception(e);
            throw e;
        }
    }

    /**
     * 得到一个数值型的序列值
     * @return 序列值
     */
    public Long nextLong() {
        return Long.parseLong(next());
    }



}
