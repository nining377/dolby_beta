package com.raincat.dolby_beta.model;

import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/15
 *     desc   : 播放列表bean
 *     version: 1.0
 * </pre>
 */
public class PlaylistDetail {
    /**
     * playlist : {"trackIds":[{"id":622720}],"id":2829821753}
     */

    private PlaylistBean playlist;

    public PlaylistBean getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlaylistBean playlist) {
        this.playlist = playlist;
    }

    public static class PlaylistBean {
        /**
         * trackIds : [{"id":622720}]
         * id : 2829821753
         */

        private long id;
        private List<TrackIdsBean> trackIds;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public List<TrackIdsBean> getTrackIds() {
            return trackIds;
        }

        public void setTrackIds(List<TrackIdsBean> trackIds) {
            this.trackIds = trackIds;
        }

        public static class TrackIdsBean {
            /**
             * id : 622720
             */

            private long id;

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }
        }
    }
}
