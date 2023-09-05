package com.alibaba.FlinkAlink;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import com.alibaba.alink.pipeline.regression.LassoRegression;
import com.alibaba.alink.pipeline.regression.LassoRegressionModel;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

//https://alinklab.cn/manual/lassoregression.html
public class LassoRegressionTest {
    @Test
    public void testLassoRegression() throws Exception {
        //创建数据集
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
        String[] colnames = new String[] {"f0", "f1"};
        //创建Lasso回归pipeline组件，并设置参数
        LassoRegression lasso = new LassoRegression()
                .setFeatureCols(colnames) //设置特征列
                .setLambda(0.1) //惩罚因子
                .setLabelCol("label")
                .setPredictionCol("pred")//设置预测列
                .enableLazyPrintModelInfo();//延迟打印模型参数（截距和theta）
        //训练数据集
        LassoRegressionModel model = lasso.fit(batchData);

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
