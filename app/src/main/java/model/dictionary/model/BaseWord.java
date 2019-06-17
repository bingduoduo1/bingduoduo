package model.dictionary.model;

public abstract class BaseWord {
    protected final String mrawdata;
    protected final NatureLanguageType mnaturetype;
    
    public BaseWord(String rawData, NatureLanguageType natureType) {
        mrawdata = rawData;
        mnaturetype = natureType;
    }
    
    public String getRawData() {
        return mrawdata;
    }
    
    public NatureLanguageType getNatureType() {
        return mnaturetype;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (null == obj)
        {
            return false;
        }
        BaseWord word = (BaseWord) obj;
        if ((this.mrawdata).equals(word.getRawData()) && this.mnaturetype == word.getNatureType())
        {
            return true;
        }
        return false;
        
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + mnaturetype.hashCode();
        hash = 31 * hash + (mrawdata == null ? 0 : mrawdata.hashCode());
        return hash;
        
    }
}
