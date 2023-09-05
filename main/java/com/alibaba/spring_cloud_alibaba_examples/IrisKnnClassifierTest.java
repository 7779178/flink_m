package com.alibaba.spring_cloud_alibaba_examples;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.dataproc.SplitBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalMultiClassBatchOp;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.MultiClassMetrics;
import com.alibaba.alink.pipeline.classification.KnnClassifier;
import org.junit.Test;

public class IrisKnnClassifierTest {
    @Test
    public void testKnnClassifier() throws Exception {
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

        //使用knn Pipeline组件，设置对应参数
        KnnClassifier knn = new KnnClassifier()
                .setFeatureCols("f0","f1","f2","f3")  //可以设置特征列
                //.setVectorCol("vec") // 设置特征向量
                .setPredictionCol("pred")
                .setPredictionDetailCol("pred_detail")
                .setLabelCol("label")
                .setK(3);
        //进行训练并测试
        BatchOperator<?> transform = knn.fit(trainData) //训练
                .transform(testData);//预测

        transform.print();

        //TODO 评估参考决策树
        MultiClassMetrics metrics2 = new EvalMultiClassBatchOp().setLabelCol("label").setPredictionCol("pred").linkFrom(transform).collectMetrics();
        System.out.println("Prefix0 accuracy:" + metrics2.getAccuracy());
        System.out.println("Macro Precision:" + metrics2.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics2.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics2.getWeightedSensitivity());

    }
}