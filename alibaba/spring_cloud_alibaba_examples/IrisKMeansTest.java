package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.dataproc.SplitBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.clustering.KMeans;
import com.alibaba.alink.pipeline.dataproc.vector.VectorAssembler;
import org.junit.Test;

public class IrisKMeansTest {
    @Test
    public void testKMeans() throws Exception {
        //构建数据集
        //构建数据集
        String URL = "C:\\Users\\hello\\IdeaProjects\\alink2103\\datas\\iris.data";
        //字段和类型  5.1,3.5,1.4,0.2,Iris-setosa
        String SCHEMA_STR = "f0 double,f1 double,f2 double,f3 double , label string";
        CsvSourceBatchOp data = new CsvSourceBatchOp()
                .setFilePath(URL)
                .setSchemaStr(SCHEMA_STR);

        //拆分数据集
        BatchOperator <?> trainData = new SplitBatchOp().setFraction(0.9);
        trainData.linkFrom(data);
        BatchOperator<?> testData = trainData.getSideOutput(0);

        testData.print();

        //创建特征向量
        VectorAssembler res = new VectorAssembler()
                .setSelectedCols("f0", "f1","f2","f3")
                .setOutputCol("vec");
        //创建Kmeans pipeline组件，设置特征向量等参数
        KMeans kmeans = new KMeans()
                .setVectorCol("vec")
                .setK(3)
                .setPredictionCol("pred");

        //创建Pipeline 组合特征向量于Kmeans组件
        Pipeline pipeline = new Pipeline().add(res).add(kmeans);

        //训练并测试
        BatchOperator<?> result = pipeline
                .fit(trainData)
                .transform(testData);

        result.print();
    }
}