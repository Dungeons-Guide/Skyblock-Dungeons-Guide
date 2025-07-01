package kr.syeyoung.modapi.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Pair<T,R> {
    public T first;
    public R second;

    public Pair(T first, R second) {
        this.first = first;
        this.second =second;
    }


}
