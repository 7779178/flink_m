package com.alibaba.FlinkAlink;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LinearRegTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LinearRegTrainBatchOpTest {
    @Test
    public void testLinearRegTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(2, 1, 1),
                Row.of(3, 2, 1),
                Row.of(4, 3, 2),
                Row.of(2, 4, 1),
                Row.of(2, 2, 1),
                Row.of(4, 3, 2),
                Row.of(1, 2, 1)
        );
        BatchOperator <?> batchData = new MemSourceBatchOp(df, "f0 int, f1 int, label int");
        //创建线性回归组件并设置参数
        BatchOperator <?> lr = new LinearRegTrainBatchOp()
                .setFeatureCols("f0", "f1")
                .setLabelCol("label")
                .lazyPrintModelInfo();
        //训练出模型
        BatchOperator model = batchData.link(lr);

        model.print("model>>>>>");

        //创建预测组件，设置预测结果列
        BatchOperator <?> predictor = new LinearRegPredictBatchOp()
                .setPredictionCol("pred");

        //测试测试集
        BatchOperator<?> result = predictor.linkFrom(model, batchData);

        //评估
        RegressionMetrics metrics = new EvalRegressionBatchOp().setPredictionCol("pred").setLabelCol("label").linkFrom(
                result).collectMetrics();
        System.out.println("Total Samples Number:" + metrics.getCount());
        System.out.println("SSE:" + metrics.getSse());
        System.out.println("SAE:" + metrics.getSae());
        System.out.println("RMSE:" + metrics.getRmse());
        System.out.println("R2:" + metrics.getR2());
    }
}