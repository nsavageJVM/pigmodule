package org.bigtop.bigpetstore.etl;


import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;
import org.bigtop.bigpetstore.contract.PetStoreStatistics;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

/**
 * This class operates by ETL'ing the dataset into pig, and then 
 * implements the "statistics" contract in the functions which follow. 
 * 
 * The pigServer is persisted through the life of the class, so that the intermediate
 * data sets created in the constructor can be reused.
 */
public class PigETL extends PetStoreStatistics {

    PigServer pigServer ;
    public PigETL(String inputPath, String outputPath, ExecType ex) throws Exception{

        System.out.println("inputPathinputPath "+inputPath);

        // run pig in local mode
        pigServer = new PigServer(ex);
        //final String datapath = test_data_directory+"/generated/part-r-00000";
 
        /** 
         * First, split the tabs up.
         * 
         * BigPetStore,storeCode_OK,2    yang,jay,Mon Dec 15 23:33:49 EST 1969,69.56,flea collar
         * 
         * ("BigPetStore,storeCode_OK,2", "yang,jay,Mon Dec 15 23:33:49 EST 1969,69.56,flea collar")
         *
         * BigPetStore,storeCode_AK,1	amanda,fitzgerald,Sat Dec 20 09:44:25 EET 1969,7.5,cat-food
         */
        pigServer.registerQuery(
                "csvdata = LOAD '<i>' AS (ID,DETAILS);"
                    .replaceAll("<i>", inputPath));



        /**
         * Now, we want to split the two tab delimited feidls into uniform fields of comma separated values.
         * To do this, we 
         * 1) Internally split the FIRST and SECOND fields by commas "a,b,c" --> (a,b,c)
         * 2) FLATTEN the FIRST and SECOND fields. (d,e) (a,b,c) -> d e a b c
         */
        pigServer.registerQuery(
                "id_details = FOREACH csvdata GENERATE " +
                        "FLATTEN" +
                            "(STRSPLIT" +
                                "(ID,',',3)) AS (drop, code, transaction) ," +
                        "FLATTEN" +
                            "(STRSPLIT" +
                                /**
                                 * Schema has to be defined here 
                                 * for any feilds which are going to export as json!
                                 */
                                "(DETAILS,',',5)) AS (lname, fname, date, price, product:chararray);");

        System.out.println(pigServer.dumpSchema("id_details"));
            /**
            * Total product sales by state:
            *   {"product":"dog treats (hard)","count":4}
            */
            pigServer.registerQuery(
                    "transactions = FOREACH id_details GENERATE $0 .. ;");
            
            pigServer.registerQuery(
                    "transactionsG = group transactions by product;");

            /**
             * pigServer.registerQuery(
                    "transactionsG = group transactions by ;");
             */
    
    }
    
    @Override
    public Map<String, Integer> numberOfTransactionsByState() throws Exception {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public Map<String, Integer> numberOfProductsByProduct() throws Exception {
        
            pigServer.registerQuery(
                    "uniqcnt  = foreach transactionsG {"+
                                   "sym = transactions.product ;"+
                                   "generate flatten(sym) as product:chararray, COUNT(sym) as count:long ;" +
                                   "};");
    
            System.out.println("Schema : " + pigServer.dumpSchema("uniqcnt"));
    
            Iterator<Tuple> tuples = pigServer.openIterator("uniqcnt");
            Map<String,Integer> ret = Maps.newHashMap();
            while(tuples.hasNext()){
                Tuple t = tuples.next();
                ret.put(t.get(0)+"",((Number)t.get(1)).intValue());
            }
            //pigServer.store("uniqcnt", outputPath, "JsonStorage");
            return ret;
    }

    
    
    
    
    
    
    
}
