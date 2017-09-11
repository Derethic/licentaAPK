package com.cti.fmi.licentaapk.models;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CategoryDisplay
{
    @SerializedName("id")
    int id;

    @SerializedName("category_name")
    String categoryName;

    @SerializedName("subcategories")
    ArrayList<SubcategoryDisplay> subcategories;

    public CategoryDisplay(int id, String categoryName, ArrayList<SubcategoryDisplay> subcategories)
    {
        this.id = id;
        this.categoryName = categoryName;
        this.subcategories = subcategories;
    }

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

    public ArrayList<SubcategoryDisplay> getSubcategories()
    {
        return subcategories;
    }

    public void setSubcategories(ArrayList<SubcategoryDisplay> subcategories)
    {
        this.subcategories = subcategories;
    }

    public static class SubcategoryDisplay
    {
        @SerializedName("id")
        int id;

        @SerializedName("subcategory_name")
        String subcategoryName;

        @SerializedName("mapcategory_id")
        int idMapCategory;

        public SubcategoryDisplay(int id, String subcategoryName, int idMapCategory)
        {
            this.id = id;
            this.subcategoryName = subcategoryName;
            this.idMapCategory = idMapCategory;
        }

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

        public int getIdMapCategory()
        {
            return idMapCategory;
        }

        public void setIdMapCategory(int idMapCategory)
        {
            this.idMapCategory = idMapCategory;
        }
    }

}
