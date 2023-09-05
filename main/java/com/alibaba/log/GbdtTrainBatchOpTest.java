package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.GbdtPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.GbdtTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GbdtTrainBatchOpTest {
    @Test
    public void testGbdtTrainBatchOp() throws Exception {
        //创建数据集
        List <Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, 0),
                Row.of(2.0, "B", 1, 1, 0),
                Row.of(3.0, "C", 2, 2, 1),
                Row.of(4.0, "D", 3, 3, 1)
        );
        BatchOperator <?> batchSource = new MemSourceBatchOp(
                df, " f0 double, f1 string, f2 int, f3 int, label int");
        //创建GBDT批组件，设置参数，并训练出模型
        BatchOperator <?> trainOp = new GbdtTrainBatchOp()
                .setLearningRate(1.0)
                .setNumTrees(3)
                .setMinSamplesPerLeaf(1)
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .linkFrom(batchSource);

        //创建测试组件设置测试列
        BatchOperator <?> predictBatchOp = new GbdtPredictBatchOp()
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred");
        //进行测试
        predictBatchOp.linkFrom(trainOp, batchSource).print();

        //TODO 评估参考决策树
    }
}
