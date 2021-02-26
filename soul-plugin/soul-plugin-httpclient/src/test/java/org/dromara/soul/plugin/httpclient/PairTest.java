package org.dromara.soul.plugin.httpclient;

import org.apache.commons.lang3.tuple.Pair;
import org.dromara.soul.common.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther : Cenjinhao
 * @date : 2021/2/26 14:24
 * @desc :
 */
public class PairTest {

    public static void main(String[] args) {
         List<Pair<String, String>> params = new ArrayList<>();
         Pair<String,String> p1 = Pair.of("int","no");
         Pair<String,String> p2 = Pair.of("java.lang.String","name");
         params.add(p1);
         params.add(p2);
         System.out.println(GsonUtils.getInstance().toJson(params));

    }
}
