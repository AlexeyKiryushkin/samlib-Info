/*
 * Copyright 2013 Dmitry Monakhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monakhv.android.samlib.sql.entity;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author monakhv
 */
public class Author  implements Serializable{
    
    protected List<Book> books;
    protected String name;
    protected long updateDate;
    protected String url;
    protected boolean isNew = false;
    protected int id;
    
    private List<Integer> tags_id;
    private List<String>  tags_name;
    private String all_tags_name;

    /**
     * Just empty constructor with empty book list and current updated time
     */
    public Author() {
        updateDate = Calendar.getInstance().getTime().getTime();
        books = new ArrayList<Book>();
        tags_id = new ArrayList<Integer>();
        tags_name = new ArrayList<String>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAll_tags_name() {
        return all_tags_name;
    }

    public void setAll_tags_name(String all_tags_name) {
        this.all_tags_name = all_tags_name;
    }

    public List<Integer> getTags_id() {
        return tags_id;
    }

    public void setTags_id(List<Integer> tags_id) {
        this.tags_id = tags_id;
    }

    public List<String> getTags_name() {
        return tags_name;
    }

    public void setTags_name(List<String> tags_name) {
        this.tags_name = tags_name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 53 * hash + this.id;
        return hash;
    }

    @SuppressWarnings({"SimplifiableIfStatement", "StringEquality"})
    @Override
    public boolean equals(Object obj) {
         if (this == obj) return true;
        if (obj == null || ((Object) this).getClass() != obj.getClass()) {
            return false;
        }
        final Author other = (Author) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        return this.id == other.id;
    }
    
    

    /**
     * Get book url to open it using web browser
     * @return String of URL to open author home page
     */
    public String getUrlForBrowser(Context context){
        SamLibConfig sc = SamLibConfig.getInstance(context);
        return sc.getAuthorUrlForBrowser(this);
    }
    
    /**
     * Making Author name as the author of the biggest book on the page
     */
    public void extractName() {
        long min = -1;
        for (Book b : books) {
            if (b.size > min) {
                name = b.authorName;
            }
        }

    }

    /**
     * Test whether we need update Author information or not
     *
     * @param newA new just downloaded author
     * @return true id we need update Author info into data base
     */
    private boolean testUpdate(Author newA) {
        
        boolean res=false;
        for (Book b : newA.books) {
            if (books.contains(b)) {//old book                
                b.setIsNew(false);
            }
            else {//new book
                res = true;
                b.setIsNew(true);
            }
        }

        return res;
    }

    /**
     * Update Author information by data of new Author object If need
     *
     * @param newA new just downloaded author
     * @return true if data is updated false in other case
     */
    public boolean update(Author newA) {
        if (testUpdate(newA)) {
            setBooks(newA.getBooks());
            setUpdateDate(newA.getUpdateDate());
            setIsNew(true);
            return true;
        }
        return false;
    }
//    public void dump(){
//        System.out.println(name);
//        System.out.println(   new Date(updateDate)      );
//
//        System.out.println("----------Begin books---------------");
//
//        for (Book b : books){
//
//            System.out.println(" - "+b.toString());
//        }
//        System.out.println("----------End   books---------------");
//    }
}
