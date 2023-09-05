package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalMultiClassBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.MultiClassMetrics;
import com.alibaba.alink.pipeline.classification.NaiveBayes;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class NaiveBayesTest {
    @Test
    public void testNaiveBayes() throws Exception {
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
        //创建朴素贝叶斯Pipeline组件，设置参数
        NaiveBayes ns = new NaiveBayes()
                .setFeatureCols("f0", "f1", "f2", "f3")
                .setLabelCol("label")
                .setPredictionCol("pred")
                .setPredictionDetailCol("pred_detail");

        BatchOperator<?> result = ns.fit(batchData)//训练
                .transform(batchData);//预测

        result.print();

        //多分类评估
        MultiClassMetrics metrics2 = new EvalMultiClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("Prefix0 accuracy:" + metrics2.getAccuracy());
        System.out.println("Macro Precision:" + metrics2.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics2.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics2.getWeightedSensitivity());

    }
}
