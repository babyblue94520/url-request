package pers.clare.urlrequest.vo;

import java.util.Arrays;

public class Data {
    private Long time;

    private String[] data;

    public Data() {
    }

    public Data(Long time, String[] data) {
        this.time = time;
        this.data = data;
    }

    public Long getTime() {
        return time;
    }

    public String[] getData() {
        return data;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data1 = (Data) o;
        return time.equals(data1.time) && Arrays.equals(data, data1.data);
    }
}
