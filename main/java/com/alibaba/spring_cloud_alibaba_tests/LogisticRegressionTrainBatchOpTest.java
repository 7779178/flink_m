package com.alibaba.spring_cloud_alibaba_tests;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LogisticRegressionTrainBatchOpTest {
    @Test
    public void testLogisticRegressionTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df_data = Arrays.asList(
                Row.of(2, 1, 1),
                Row.of(3, 2, 1),
                Row.of(4, 3, 2),
                Row.of(2, 4, 1),
                Row.of(2, 2, 1),
                Row.of(4, 3, 2),
                Row.of(1, 2, 1),
                Row.of(5, 3, 2)
        );
        BatchOperator <?> input = new MemSourceBatchOp(df_data, "f0 int, f1 int, label int");
        BatchOperator dataTest = input;
        //创建逻辑回归批组件，设置特征列
        BatchOperator <?> lr = new LogisticRegressionTrainBatchOp().setFeatureCols("f0", "f1").setLabelCol("label");
        //训练
        BatchOperator model = input.link(lr);
        //创建预测组件，设置预测列
        BatchOperator <?> predictor = new LogisticRegressionPredictBatchOp().setPredictionCol("pred");
        //进行预测
        BatchOperator<?> result = predictor.linkFrom(model, dataTest);

        result.print();

        //TODO 评估参考决策树的二分类评估 和多分类评估
    }
}
