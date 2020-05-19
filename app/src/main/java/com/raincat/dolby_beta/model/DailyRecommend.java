package com.raincat.dolby_beta.model;

import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/15
 *     desc   : 每日推荐歌单
 *     version: 1.0
 * </pre>
 */
public class DailyRecommend {
    private List<RecommendBean> recommend;

    public List<RecommendBean> getRecommend() {
        return recommend;
    }

    public void setRecommend(List<RecommendBean> recommend) {
        this.recommend = recommend;
    }

    public static class RecommendBean {
        /**
         * id : 2829821753
         * name : [聚精会神] 工作学习必备纯音乐
         */

        private long id;
        private String name;

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
