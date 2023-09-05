package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.classification.RandomForestClassifier;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RandomForestClassifierTest {
    @Test
    public void testRandomForestClassifier() throws Exception {
        //创建数据集
        List<Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, 0),
                Row.of(2.0, "B", 1, 1, 0),
                Row.of(3.0, "C", 2, 2, 1),
                Row.of(4.0, "D", 3, 3, 1)
        );
        BatchOperator<?> batchSource
                = new MemSourceBatchOp(df, " f0 double, f1 string, f2 int, f3 int, label int");

        //创建随机森林Pipeline组件并对训练集进行训练，训练出模型后对测试集进行测试
        BatchOperator<?> result = new RandomForestClassifier()
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred")
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .fit(batchSource) //训练
                .transform(batchSource);//测试

        result.print();

        //TODO 评估

    }
}
