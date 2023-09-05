package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.NaiveBayesPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.NaiveBayesTrainBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalMultiClassBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.MultiClassMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class NaiveBayesTrainBatchOpTest {
    @Test
    public void testNaiveBayesTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df_data = Arrays.asList(
                Row.of(1.0, 1.0, 0.0, 1.0, 1),
                Row.of(1.0, 0.0, 1.0, 1.0, 1),
                Row.of(1.0, 0.0, 1.0, 1.0, 1),
                Row.of(0.0, 1.0, 1.0, 0.0, 0),
                Row.of(0.0, 1.0, 1.0, 0.0, 0),
                Row.of(0.0, 1.0, 1.0, 0.0, 0),
                Row.of(0.0, 1.0, 1.0, 0.0, 0),
                Row.of(1.0, 1.0, 1.0, 1.0, 1),
                Row.of(0.0, 1.0, 1.0, 0.0, 0)
        );
        BatchOperator <?> batchData = new MemSourceBatchOp(df_data,
                "f0 double, f1 double, f2 double, f3 double, label int");
        //创建朴素贝叶斯批组件设置参数
        BatchOperator <?> ns = new NaiveBayesTrainBatchOp().setFeatureCols("f0", "f1", "f2", "f3")
                .setLabelCol("label");
        //训练出模型
        BatchOperator model = batchData.link(ns);

        //创建预测组件，设置预测列
        BatchOperator <?> predictor = new NaiveBayesPredictBatchOp().setPredictionCol("pred").setPredictionDetailCol("pred_detail");
        //进行预测
        BatchOperator<?> result = predictor.linkFrom(model, batchData);
        //TODO 评估 参考决策树
        //多分类评估
        MultiClassMetrics metrics2 = new EvalMultiClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("accuracy:" + metrics2.getAccuracy());
        System.out.println("Macro Precision:" + metrics2.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics2.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics2.getWeightedSensitivity());

    }
}