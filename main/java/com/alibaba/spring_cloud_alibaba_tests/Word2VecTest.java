package com.alibaba.spring_cloud_alibaba_tests;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.nlp.Word2Vec;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class Word2VecTest {
    @Test
    public void testWord2Vec() throws Exception {
        List <Row> df = Arrays.asList(
                Row.of("A B C")
        );
        BatchOperator <?> inOp = new MemSourceBatchOp(df, "tokens string");

        Word2Vec word2vec = new Word2Vec().setSelectedCol("tokens").setMinCount(1).setVectorSize(4);

        word2vec.fit(inOp).transform(inOp).print();
    }
}
