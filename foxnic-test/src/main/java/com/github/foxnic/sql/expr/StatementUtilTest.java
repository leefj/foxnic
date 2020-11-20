package com.github.foxnic.sql.expr;

import org.junit.Test;

import com.github.foxnic.dao.sql.SQLParserUtil;
import com.github.foxnic.dao.sql.StatementUtil;
import com.github.foxnic.sql.dialect.SQLDialect;

public class StatementUtilTest {
	
	String[] inserts= {
			"insert into sys_menu (menu_id,menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)\r\n" + 
			"values(seq_sys_menu.nextval,'模块', '3', '1', '/workflow/wfModel', 'C', '0', 'workflow:wfModel:view', '#', 'admin', SYSDATE, 'admin', SYSDATE, '模块菜单');"
	};
	
	@Test
	public void insert_parse_test()
	{
	 
 
		Insert insert0=StatementUtil.parseInsert(inserts[0],SQLDialect.PLSQL);
		
		Insert insert1=StatementUtil.parseInsert(insert0.getSQL(),SQLDialect.PLSQL);
		
		System.out.println(insert0.getSQL());
		System.out.println(insert1.getSQL());
		
	}

}
