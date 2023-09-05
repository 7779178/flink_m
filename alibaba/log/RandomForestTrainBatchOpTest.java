package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.RandomForestPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.RandomForestTrainBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalMultiClassBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.MultiClassMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RandomForestTrainBatchOpTest {
    @Test
    public void testRandomForestTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, 0),
                Row.of(2.0, "B", 1, 1, 0),
                Row.of(3.0, "C", 2, 2, 1),
                Row.of(4.0, "D", 3, 3, 1)
        );
        BatchOperator <?> batchSource = new MemSourceBatchOp(
                df, " f0 double, f1 string, f2 int, f3 int, label int");

        //创建随机森林组件 设置参数并训练出模型
        BatchOperator <?> trainOp = new RandomForestTrainBatchOp()
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .linkFrom(batchSource);

        //创建测试组件，设置测试列，并进行测试
        BatchOperator <?> predictBatchOp = new RandomForestPredictBatchOp()
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred");
        BatchOperator<?> result = predictBatchOp.linkFrom(trainOp, batchSource);
        result.print();


        //评估
        MultiClassMetrics metrics2 = new EvalMultiClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("Prefix0 accuracy:" + metrics2.getAccuracy());
        System.out.println("Macro Precision:" + metrics2.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics2.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics2.getWeightedSensitivity());

    }
}
