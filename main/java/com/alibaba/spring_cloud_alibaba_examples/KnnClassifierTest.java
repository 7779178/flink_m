package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.classification.KnnClassifier;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KnnClassifierTest {
    @Test
    public void testKnnClassifier() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1, "0,0,0"),
                Row.of(1, "0.1,0.1,0.1"),
                Row.of(1, "0.2,0.2,0.2"),
                Row.of(0, "9,9,9"),
                Row.of(0, "9.1,9.1,9.1"),
                Row.of(0, "9.2,9.2,9.2")
        );
        BatchOperator <?> dataSource = new MemSourceBatchOp(df, "label int,  vec string");
        //使用knn Pipeline组件，设置对应参数
        KnnClassifier knn = new KnnClassifier()
                //.setFeatureCols("f0","f1")  //可以设置特征列
                .setVectorCol("vec") // 设置特征向量
                .setPredictionCol("pred")
                .setLabelCol("label")
                .setK(3);
        //进行训练并测试
        BatchOperator<?> transform = knn.fit(dataSource) //训练
                .transform(dataSource);//预测

        transform.print();

        //TODO 评估参考决策树

    }
}