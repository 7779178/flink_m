package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.clustering.KMeansPredictBatchOp;
import com.alibaba.alink.operator.batch.clustering.KMeansTrainBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalClusterBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.ClusterMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KMeansTrainBatchOpTest {
    @Test
    public void testKMeansTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(0, "0 0 0"),
                Row.of(1, "0.1,0.1,0.1"),
                Row.of(2, "0.2,0.2,0.2"),
                Row.of(3, "9 9 9"),
                Row.of(4, "9.1 9.1 9.1"),
                Row.of(5, "9.2 9.2 9.2")
        );
        BatchOperator <?> inOp1 = new MemSourceBatchOp(df, "id int, vec string");
        //创建kmeans批组件，设置特征向量，并训练出模型
        BatchOperator <?> kmeans = new KMeansTrainBatchOp()
                .setVectorCol("vec")
                //.setK(2) //设置分成多少类，默认分2类
                .linkFrom(inOp1);

        kmeans.lazyPrint(10);

        //创建预测组件并设置预测列，进行测试
        BatchOperator <?> result = new KMeansPredictBatchOp()
                .setPredictionCol("pred")
                .linkFrom(kmeans, inOp1);
        result.print();
        //评估
        ClusterMetrics metrics = new EvalClusterBatchOp().setVectorCol("vec").setPredictionCol("pred").linkFrom(result).collectMetrics();
        System.out.println("Total Samples Number:" + metrics.getCount());
        System.out.println("Cluster Number:" + metrics.getK());
        System.out.println("Cluster Array:" + Arrays.toString(metrics.getClusterArray()));
        System.out.println("Cluster Count Array:" + Arrays.toString(metrics.getCountArray()));
        System.out.println("CP:" + metrics.getCp());
        System.out.println("DB:" + metrics.getDb());
        System.out.println("SP:" + metrics.getSp());
        System.out.println("SSB:" + metrics.getSsb());
        System.out.println("SSW:" + metrics.getSsw());
        System.out.println("CH:" + metrics.getVrc());
        System.out.println("Purity:" + metrics.getPurity());


    }
}
