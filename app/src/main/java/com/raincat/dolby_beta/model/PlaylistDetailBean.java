package com.raincat.dolby_beta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/04/15
 *     desc   : 播放列表bean
 *     version: 1.0
 * </pre>
 */
public class PlaylistDetailBean {
    private PlaylistBean playlist;

    public PlaylistBean getPlaylist() {
        if (playlist == null)
            playlist = new PlaylistBean();
        return playlist;
    }

    public void setPlaylist(PlaylistBean playlist) {
        this.playlist = playlist;
    }

    public static class PlaylistBean {
        private long id = 0L;
        private List<TrackIdsBean> trackIds;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public List<TrackIdsBean> getTrackIds() {
            if (trackIds == null)
                trackIds = new ArrayList<>();
            return trackIds;
        }

        public void setTrackIds(List<TrackIdsBean> trackIds) {
            this.trackIds = trackIds;
        }

        public static class TrackIdsBean {
            private long id = 0L;

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }
        }
    }
}