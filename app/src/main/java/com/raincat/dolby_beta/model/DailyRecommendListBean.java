package com.raincat.dolby_beta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/15
 *     desc   : 每日推荐歌单
 *     version: 1.0
 * </pre>
 */
public class DailyRecommendListBean {
    private List<RecommendBean> recommend;

    public List<RecommendBean> getRecommend() {
        if (recommend == null)
            recommend = new ArrayList<>();
        return recommend;
    }

    public void setRecommend(List<RecommendBean> recommend) {
        this.recommend = recommend;
    }

    public static class RecommendBean {
        private long id = 0L;
        private String name = "";

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}