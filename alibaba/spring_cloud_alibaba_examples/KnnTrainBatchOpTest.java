package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.KnnPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.KnnTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KnnTrainBatchOpTest {
    @Test
    public void testKnnTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1, 0, 0),
                Row.of(2, 8, 8),
                Row.of(1, 1, 2),
                Row.of(2, 9, 10),
                Row.of(1, 3, 1),
                Row.of(2, 10, 7)
        );
        BatchOperator <?> dataSourceOp = new MemSourceBatchOp(df, "label int, f0 int, f1 int");

        //第一次训练和测试
        //创建knn批主键设置参数并训练
        BatchOperator <?> trainOp = new KnnTrainBatchOp()
                .setFeatureCols("f0", "f1")
                //.setVectorCol("")
                .setLabelCol("label")
                .setDistanceType("EUCLIDEAN")
                .linkFrom(dataSourceOp); //训练训练集
        //创建预测组件设置预测列并进行预测
        BatchOperator <?> predictOp = new KnnPredictBatchOp().setPredictionCol("pred").setK(4).linkFrom(trainOp, dataSourceOp); //预测
        predictOp.print();


        //预测
        List <Row> df2 = Arrays.asList(
                Row.of(0,0, 0),
                Row.of(0,8, 8),
                Row.of(0,1, 2),
                Row.of(0,9, 10),
                Row.of(0,3, 1),
                Row.of(0,10, 7)
        );
        BatchOperator <?> test2 = new MemSourceBatchOp(df, "ff int,f0 int, f1 int");
        BatchOperator <?> pred2 = new KnnPredictBatchOp().setPredictionCol("pred").setK(4).linkFrom(trainOp, test2); //预测
        pred2.print();

    }
}