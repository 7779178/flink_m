package com.alibaba.log;


//https://alinklab.cn/manual/decisiontreetrainbatchop.html

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.DecisionTreePredictBatchOp;
import com.alibaba.alink.operator.batch.classification.DecisionTreeTrainBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalBinaryClassBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.BinaryClassMetrics;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.classification.DecisionTreePredictStreamOp;
import com.alibaba.alink.operator.stream.source.MemSourceStreamOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

//https://alinklab.cn/manual/decisiontreetrainbatchop.html、
//训练的时候是批数据集
//进行测试的时候可以是批 也可以是流
public class DecisionTreeTrainBatchOpTest {
    @Test
    public void t1() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, "输"),
                Row.of(2.0, "B", 1, 1, "输"),
                Row.of(3.0, "C", 2, 2, "赢"),
                Row.of(4.0, "D", 3, 3, "赢")
        );
        //批数据集（）
        BatchOperator <?> batchSource = new MemSourceBatchOp(df, " f0 double, f1 string, f2 int, f3 int, label string");
        //创建决策树批组件，设置对应参数，训练批数据集，返回模型
        BatchOperator <?> trainOp = new DecisionTreeTrainBatchOp()
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .linkFrom(batchSource);//训练训练集

        //根据模型进行预测：(批)
        BatchOperator <?> predictBatchOp = new DecisionTreePredictBatchOp()
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred");
        BatchOperator<?> result = predictBatchOp.linkFrom(trainOp, batchSource); //(模型,测试集)
        result.print();

        //f0 |f1 |f2 |f3 |label|pred|pred_detail
        //---|---|---|---|-----|----|-----------
        //4.0000|D|3|3|1|1|{"0":0.0,"1":1.0}
        //2.0000|B|1|1|0|0|{"0":1.0,"1":0.0}
        //1.0000|A|0|0|0|0|{"0":1.0,"1":0.0}
        //3.0000|C|2|2|1|1|{"0":0.0,"1":1.0}

        //评估
        //EvalBinaryClassBatchOp
        BinaryClassMetrics metrics = new EvalBinaryClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("AUC:" + metrics.getAuc());
        System.out.println("KS:" + metrics.getKs());
        System.out.println("PRC:" + metrics.getPrc());
        System.out.println("Accuracy:" + metrics.getAccuracy());
        System.out.println("Macro Precision:" + metrics.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics.getWeightedSensitivity());


        //保存模型 TODO


    }

    //用批数据集去训练，用于预测流
    @Test
    public void t2() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, 0),
                Row.of(2.0, "B", 1, 1, 0),
                Row.of(3.0, "C", 2, 2, 1),
                Row.of(4.0, "D", 3, 3, 1)
        );
        //批数据集（）
        BatchOperator <?> batchSource = new MemSourceBatchOp(df, " f0 double, f1 string, f2 int, f3 int, label int");
        //流数据
        StreamOperator <?> streamSource = new MemSourceStreamOp(df, " f0 double, f1 string, f2 int, f3 int, label int");
        //创建决策树批组件，设置对应参数，训练批数据集，返回模型
        BatchOperator <?> trainOp = new DecisionTreeTrainBatchOp()
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .linkFrom(batchSource);//训练训练集
        //根据模型进行预测：(流)
        StreamOperator <?> predictStreamOp = new DecisionTreePredictStreamOp(trainOp)
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred");
        predictStreamOp.linkFrom(streamSource).print();

        StreamOperator.execute();
    }
}
