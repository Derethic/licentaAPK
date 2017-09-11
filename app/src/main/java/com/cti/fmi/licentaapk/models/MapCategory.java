package com.cti.fmi.licentaapk.models;

import com.google.gson.annotations.SerializedName;

public class MapCategory
{
    @SerializedName("id")
    private int id;

    @SerializedName("category")
    private Category category;

    @SerializedName("subcategory")
    private Subcategory subcategory;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public Subcategory getSubcategory()
    {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory)
    {
        this.subcategory = subcategory;
    }

    protected class Category
    {
        @SerializedName("id")
        int id;

        @SerializedName("category_name")
        String categoryName;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getCategoryName()
        {
            return categoryName;
        }

        public void setCategoryName(String categoryName)
        {
            this.categoryName = categoryName;
        }
    }

    protected class Subcategory
    {
        @SerializedName("id")
        int id;

        @SerializedName("subcategory_name")
        String subcategoryName;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getSubcategoryName()
        {
            return subcategoryName;
        }

        public void setSubcategoryName(String subcategoryName)
        {
            this.subcategoryName = subcategoryName;
        }
    }

}
