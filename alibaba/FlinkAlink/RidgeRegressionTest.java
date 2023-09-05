package com.alibaba.FlinkAlink;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import com.alibaba.alink.pipeline.regression.RidgeRegression;
import com.alibaba.alink.pipeline.regression.RidgeRegressionModel;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

//https://alinklab.cn/manual/ridgeregression.html
public class RidgeRegressionTest {
    @Test
    public void testRidgeRegression() throws Exception {
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

        //创建岭回归Pipeline算法组件，设置参数
        String[] colnames = new String[] {"f0", "f1"};
        RidgeRegression ridge = new RidgeRegression()
                .setFeatureCols(colnames) //设置特征列
                .setLambda(0.1)// 设置惩罚因子
                .setLabelCol("label")
                .setPredictionCol("pred") //设置预测列
                .enableLazyPrintModelInfo(); //延迟打印模型信息（截距  theta）

        //训练数据集
        RidgeRegressionModel model = ridge.fit(batchData);

        //使用模型对测试集进行测试
        BatchOperator<?> result = model.transform(batchData);

        result.print();
        //评估 https://alinklab.cn/manual/evalregressionbatchop.html
        RegressionMetrics metrics = new EvalRegressionBatchOp().setPredictionCol("pred").setLabelCol("label").linkFrom(result).collectMetrics();
        System.out.println("Total Samples Number:" + metrics.getCount());
        System.out.println("SSE:" + metrics.getSse());
        System.out.println("SAE:" + metrics.getSae());
        System.out.println("RMSE:" + metrics.getRmse());
        System.out.println("R2:" + metrics.getR2());
        System.out.println("MSE:" + metrics.getMse());

    }
}
