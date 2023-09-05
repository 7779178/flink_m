package com.alibaba.spring_cloud_alibaba_tests;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.nlp.DocWordCountBatchOp;
import com.alibaba.alink.operator.batch.nlp.SegmentBatchOp;
import com.alibaba.alink.operator.batch.nlp.StopWordsRemoverBatchOp;
import com.alibaba.alink.operator.batch.nlp.TfidfBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TfidfBatchOpTest {
    @Test
    public void testTfidfBatchOp() throws Exception {
        //构建数据集
        List <Row> df = Arrays.asList(
                Row.of(0, "二手旧书:医学电磁成像"),
                Row.of(1, "二手美国文学选读（ 下册 ）李宜燮南开大学出版社 9787310003969"),
                Row.of(2, "二手正版图解象棋入门/谢恩思主编/华龄出版社"),
                Row.of(3, "二手中国糖尿病文献索引"),
                Row.of(4, "二手郁达夫文集（ 国内版 ）全十二册馆藏书")
        );
        BatchOperator <?> inOp1 = new MemSourceBatchOp(df, "id int, text string");

        BatchOperator <?> segment = new SegmentBatchOp().setSelectedCol("text").setOutputCol("segment").linkFrom(inOp1); //分词
        BatchOperator <?> remover = new StopWordsRemoverBatchOp().setSelectedCol("segment").setOutputCol("remover").linkFrom(segment); //停用词过滤
        BatchOperator <?> wordCount = new DocWordCountBatchOp().setContentCol("remover").setDocIdCol("id").linkFrom(remover); //	文本词频统计

        wordCount.print();
        BatchOperator <?> tfidf = new TfidfBatchOp().setDocIdCol("id").setWordCol("word").setCountCol("cnt").linkFrom(wordCount); //评估

        tfidf.print();
    }
}