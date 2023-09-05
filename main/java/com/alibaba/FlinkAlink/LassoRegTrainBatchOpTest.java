package com.alibaba.FlinkAlink;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.regression.LassoRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.LassoRegTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

//https://alinklab.cn/manual/lassoregtrainbatchop.html
public class LassoRegTrainBatchOpTest {
    @Test
    public void testLassoRegTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(2, 1, 1),
                Row.of(3, 2, 1),
                Row.of(4, 3, 2),
                Row.of(2, 4, 1),
                Row.of(2, 2, 1),
                Row.of(4, 3, 2),
                Row.of(1, 2, 1),
                Row.of(5, 3, 3)
        );
        BatchOperator <?> batchData = new MemSourceBatchOp(df, "f0 int, f1 int, label int");
        //创建Lasso回归算法组件，并设置参数
        BatchOperator <?> lasso = new LassoRegTrainBatchOp()
                .setLambda(0.1) //惩罚因子
                .setFeatureCols("f0", "f1") //特征列
                .setLabelCol("label")
                .lazyPrintModelInfo(); //打印模型参数（包括截距和theta）

        //进行训练
        BatchOperator model = batchData.link(lasso);

        //准备测试，先创建一个测试组件，设置测试列
        BatchOperator <?> predictor = new LassoRegPredictBatchOp().setPredictionCol("pred");

        //进行测试
        BatchOperator<?> result = predictor.linkFrom(model, batchData);
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