package com.github.foxnic.dao.spec;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.busi.id.SequenceType;
import com.github.foxnic.commons.collection.MapUtil;
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

    private static final Set<String> sequenceIds=new HashSet<>();
    private static final ConcurrentHashMap<String,Integer> sequenceFetchSize=new ConcurrentHashMap<String,Integer>();
    private static final ConcurrentHashMap<String,ConcurrentLinkedQueue<String>> sequencesPool=new ConcurrentHashMap<String,ConcurrentLinkedQueue<String>>();


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

        Integer fetchSize=sequenceFetchSize.get(key);
        if(fetchSize==null) {
            fetchSize=dao.queryInteger("select fetch_size from "+table+" where id=? and tenant_id=?",id,this.tenantId);
            if(fetchSize==null) fetchSize=4;
            sequenceFetchSize.put(key,fetchSize);
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
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (this.exists()) {
                return true;
            } else {
                e.printStackTrace();
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
        if (sequenceIds.contains(key)) {
            return true;
        }
        boolean ex=existInTable();
        if (ex) {
            sequenceIds.add(key);
        }
        return ex;
    }

    public void setFetchSize(int size) {
        sequenceFetchSize.put(key, size);
        dao.execute("update "+table+" set fetch_size=? where id=? and tenant_id=?",size,id,this.tenantId);
    }

    public Integer getFetchSize() {
        Integer size=sequenceFetchSize.get(key);
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
        ConcurrentLinkedQueue<String> queue=sequencesPool.get(key);
        if(queue==null) {
            queue = new ConcurrentLinkedQueue<String>();
            sequencesPool.put(key,queue);
        }
        String val=null;
        try {
            val=queue.remove();
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
                    sequenceIds.remove(key);
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
            if(StringUtil.isEmpty(vals)) {
                if(!existInTable()){
                    sequenceIds.remove(key);
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
