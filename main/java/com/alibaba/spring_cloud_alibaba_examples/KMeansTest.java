package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.clustering.KMeans;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KMeansTest {
    @Test
    public void testKMeans() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(0, "0 0 0"),
                Row.of(1, "0.1,0.1,0.1"),
                Row.of(2, "0.2,0.2,0.2"),
                Row.of(3, "9 9 9"),
                Row.of(4, "9.1 9.1 9.1"),
                Row.of(5, "9.2 9.2 9.2")
        );
        BatchOperator <?> inOp = new MemSourceBatchOp(df, "id int, vec string");
        //创建Kmeans pipeline组件，设置特征向量等参数
        KMeans kmeans = new KMeans()
                .setVectorCol("vec")
                .setK(2)
                .setPredictionCol("pred");
        //训练并测试
        BatchOperator<?> result = kmeans
                .fit(inOp)
                .transform(inOp);

        result.print();
    }
}