package org.adorsys.encobject.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tuple<X, Y> {
    private final X x;
    private final Y y;
}
