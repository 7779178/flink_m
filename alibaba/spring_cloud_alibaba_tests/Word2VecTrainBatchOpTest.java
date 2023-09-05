package com.alibaba.spring_cloud_alibaba_tests;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.nlp.Word2VecPredictBatchOp;
import com.alibaba.alink.operator.batch.nlp.Word2VecTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class Word2VecTrainBatchOpTest {
    @Test
    public void testWord2VecTrainBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of("A B C C")
        );
        BatchOperator <?> inOp1 = new MemSourceBatchOp(df, "tokens string");

        //使用Word2Vec批组件进行训练
        BatchOperator <?> train = new Word2VecTrainBatchOp().setSelectedCol("tokens").setMinCount(1).setVectorSize(4)
                .linkFrom(inOp1);

        //预测
        BatchOperator <?> predictBatch = new Word2VecPredictBatchOp().setSelectedCol("tokens").linkFrom(train, inOp1);
        train.lazyPrint(-1);
        predictBatch.print();

    }
}