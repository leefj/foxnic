package com.github.foxnic.generator.util;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleCodeGenerator {

    private int gt=0;

    private List<ModuleCodeConfig> configs=new ArrayList<>();

    public void addConfig(ModuleCodeConfig... cfg) {
        configs.addAll(Arrays.asList(cfg));
    }




    public void start() {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while(true) {
                gt++;
                List<String> list=new ArrayList<>();
                for (int i = 0; i < configs.size(); i++) {
                    DBTable table =BeanUtil.getFieldValue(configs.get(i),"TABLE",DBTable.class);
                    list.add("("+(i+1)+")"+table.name());
                }

                System.out.println("\n"+gt+" - 请输入需模块序号: "+ StringUtil.join(list,"\t"));
                String str = br.readLine();
                Integer index= DataParser.parseInteger(str);
                if(index==null) {
                    System.err.println("请输入数字序号");
                    continue;
                }
                index=index-1;
                if(index<0 || index>=configs.size()) {
                    System.err.println("序号不存在");
                    continue;
                }

                ModuleContext context=configs.get(index).config();
                context.buildAll();
                System.out.println("\n"+context.getTopic()+" 代码已经生成");
                context.getListPageHTMLFile().save();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
