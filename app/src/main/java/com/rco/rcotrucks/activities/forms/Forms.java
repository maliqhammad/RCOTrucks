package com.rco.rcotrucks.activities.forms;

import java.util.List;

public class Forms {
    public String formsHeader;
    public List<FormField> formsList;


    public Forms(String header, List<FormField> list) {
        formsHeader = header;
        formsList = list;

    }
}
