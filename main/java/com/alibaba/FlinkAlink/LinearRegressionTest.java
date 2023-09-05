package com.alibaba.FlinkAlink;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import com.alibaba.alink.pipeline.regression.LinearRegression;
import com.alibaba.alink.pipeline.regression.LinearRegressionModel;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LinearRegressionTest {
    @Test
    public void testLinearRegression() throws Exception {
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
        String[] colnames = new String[] {"f0", "f1"};
        LinearRegression lr = new LinearRegression()
                .setFeatureCols(colnames)
                .setLabelCol("label")
                .setPredictionCol("pred")//设置预测结果列
                .enableLazyPrintModelInfo();

        //训练出模型
        LinearRegressionModel model = lr.fit(batchData);

        model.getModelData().print("model>>>>");
        //测试测试集
        BatchOperator<?> result = model.transform(batchData);
        //评估
        RegressionMetrics metrics1 = new EvalRegressionBatchOp().setPredictionCol("pred").setLabelCol("label").linkFrom(result).collectMetrics();
        System.out.println("metrics" + metrics1);

        //评估
        //RegressionMetrics metrics2 = result.link(new EvalRegressionBatchOp().setLabelCol("label").setPredictionCol("pred").lazyPrintMetrics("LinearRegression")).collectMetrics();
    }
}