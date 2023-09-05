package com.alibaba.FlinkAlink;


import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalRegressionBatchOp;
import com.alibaba.alink.operator.batch.regression.RidgeRegPredictBatchOp;
import com.alibaba.alink.operator.batch.regression.RidgeRegTrainBatchOp;
import com.alibaba.alink.operator.batch.sink.AkSinkBatchOp;
import com.alibaba.alink.operator.batch.source.AkSourceBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.RegressionMetrics;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SaveModeTest1 {
    @Test
    public void testRidgeRegTrainBatchOp() throws Exception {
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
        //使用岭回归算法组件进行训练，设置训练参数
        BatchOperator <?> ridge = new RidgeRegTrainBatchOp()
                .setLambda(0.1) //惩罚因子，稀疏因子
                .setFeatureCols("f0", "f1") //特征列
                .setLabelCol("label")
                .lazyPrintModelInfo() //打印模型信息，包括截距和theta
                ; //label

        //进行训练，得到模型
        BatchOperator model = batchData.link(ridge);
        model.print();

        //进行测试，首先创建测试组件，设置测试列
        BatchOperator <?> predictor = new RidgeRegPredictBatchOp().setPredictionCol("pred");
        //使用模型对测试集进行测试
        BatchOperator<?> result = predictor.linkFrom(model, batchData);

        result.print();

        //评估：
        RegressionMetrics metrics = new EvalRegressionBatchOp().setPredictionCol("pred").setLabelCol("label").linkFrom(result).collectMetrics();
        System.out.println("Total Samples Number:" + metrics.getCount());
        System.out.println("SSE:" + metrics.getSse());
        System.out.println("SAE:" + metrics.getSae());
        System.out.println("RMSE:" + metrics.getRmse());
        System.out.println("MSE:" + metrics.getMse());
        System.out.println("R2:" + metrics.getR2());

        //模型保存
        //CsvSinkBatchOp sink = new CsvSinkBatchOp().setFilePath("datas/ridge_model1");
        AkSinkBatchOp sink = new AkSinkBatchOp().setFilePath("datas/ridge_model2");
        model.link(sink);
        BatchOperator.execute();

    }


    @Test
    public void getModel() throws Exception {
        //取出模型
        //String schema = "model_id int, model_info string, label_value double";
        //CsvSourceBatchOp model = new CsvSourceBatchOp().setFilePath("datas/ridge_model1").setSchemaStr(schema).setFieldDelimiter(",");
        AkSourceBatchOp model = new AkSourceBatchOp().setFilePath("datas/ridge_model2");
        model.print();

        //创建测试集
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

        BatchOperator <?> predictor = new RidgeRegPredictBatchOp().setPredictionCol("pred");
        //使用模型对测试集进行测试
        BatchOperator<?> result = predictor.linkFrom(model, batchData);
        result.print();

    }


}
