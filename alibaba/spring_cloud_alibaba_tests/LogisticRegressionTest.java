package com.alibaba.spring_cloud_alibaba_tests;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.classification.LogisticRegression;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LogisticRegressionTest {
    @Test
    public void testLogisticRegression() throws Exception {
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
        BatchOperator <?> batchData = new MemSourceBatchOp(df_data, "f0 int, f1 int, label int");
        //创建逻辑回归 Pipeline组件，设置参数
        LogisticRegression lr = new LogisticRegression().setFeatureCols("f0", "f1").setLabelCol("label")
                .setPredictionCol("pred");

        BatchOperator<?> result = lr.fit(batchData)//训练
                .transform(batchData);//预测

        result.print();

        //TODO 评估参考决策树的二分类评估和多分类评估

    }
}
