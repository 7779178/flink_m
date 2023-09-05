package com.alibaba.log;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.evaluation.EvalBinaryClassBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalMultiClassBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.evaluation.BinaryClassMetrics;
import com.alibaba.alink.operator.common.evaluation.MultiClassMetrics;
import com.alibaba.alink.pipeline.classification.DecisionTreeClassifier;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DecisionTreeClassifierTest {
    @Test
    public void testDecisionTreeClassifier() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(1.0, "A", 0, 0, 0),
                Row.of(2.0, "B", 1, 1, 0),
                Row.of(3.0, "C", 2, 2, 1),
                Row.of(4.0, "D", 3, 3, 1)
        );
        BatchOperator <?> batchSource = new MemSourceBatchOp(df, " f0 double, f1 string, f2 int, f3 int, label int");
        //创建决策树Pipeline组件，设置阐述并训练 并测试
        BatchOperator<?> result = new DecisionTreeClassifier()
                .setPredictionDetailCol("pred_detail")
                .setPredictionCol("pred")
                .setLabelCol("label")
                .setFeatureCols("f0", "f1", "f2", "f3")
                .fit(batchSource) //训练
                .transform(batchSource);//测试

        result.print();
        //f0 |f1 |f2 |f3 |label|pred|pred_detail
        //---|---|---|---|-----|----|-----------
        //1.0000|A|0|0|0|0|{"0":1.0,"1":0.0}
        //2.0000|B|1|1|0|0|{"0":1.0,"1":0.0}
        //3.0000|C|2|2|1|1|{"0":0.0,"1":1.0}
        //4.0000|D|3|3|1|1|{"0":0.0,"1":1.0}

        //对结果进行评估(二分类评估)
        BinaryClassMetrics metrics = new EvalBinaryClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("AUC:" + metrics.getAuc());
        System.out.println("KS:" + metrics.getKs());
        System.out.println("PRC:" + metrics.getPrc());
        System.out.println("Accuracy:" + metrics.getAccuracy());
        System.out.println("Macro Precision:" + metrics.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics.getWeightedSensitivity());


        //多分类评估
        MultiClassMetrics metrics2 = new EvalMultiClassBatchOp().setLabelCol("label").setPredictionDetailCol("pred_detail").linkFrom(result).collectMetrics();
        System.out.println("Prefix0 accuracy:" + metrics2.getAccuracy());
        System.out.println("Macro Precision:" + metrics2.getMacroPrecision());
        System.out.println("Micro Recall:" + metrics2.getMicroRecall());
        System.out.println("Weighted Sensitivity:" + metrics2.getWeightedSensitivity());


        //模型保存 TODO

    }
}