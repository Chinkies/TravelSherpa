package com.example.project_application_mobile;

import java.util.List;

public class OverpassResponse {
    public List<Element> elements;

    public static class Element {
        public long id;
        public String type;
        public double lat;
        public double lon;
        public Center center;
        public Tags tags;

    }

    public static class Center {
        public double lat;
        public double lon;
    }

    public static class Tags {
        public String name;
        public String tourism;
        public String historic;
        public String leisure;
        public String amenity;
        public String wikipedia;
        public String opening_hours;
    }
}