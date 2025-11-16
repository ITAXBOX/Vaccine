package com.vaccine.enumeration;

import com.vaccine.core.ScanContext;
import com.vaccine.model.Parameter;
import com.vaccine.model.ScanResult;
import com.vaccine.model.Target;

public interface UnionBasedEnumerator {
    void enumerate(ScanContext context, ScanResult result, Target target, Parameter vulnParam);
}
