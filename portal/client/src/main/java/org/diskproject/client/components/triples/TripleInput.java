package org.diskproject.client.components.triples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diskproject.client.components.customise.GWTCodeMirror;
import org.diskproject.client.rest.AppNotification;
import org.diskproject.client.rest.DiskREST;
import org.diskproject.shared.classes.common.Triple;
import org.diskproject.shared.classes.vocabulary.Individual;
import org.diskproject.shared.classes.vocabulary.Property;
import org.diskproject.shared.classes.vocabulary.Type;
import org.diskproject.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import edu.stanford.bmir.gwtcodemirror.client.AutoCompletionCallback;
import edu.stanford.bmir.gwtcodemirror.client.AutoCompletionChoice;
import edu.stanford.bmir.gwtcodemirror.client.AutoCompletionHandler;
import edu.stanford.bmir.gwtcodemirror.client.AutoCompletionResult;
import edu.stanford.bmir.gwtcodemirror.client.EditorPosition;

public class TripleInput extends GWTCodeMirror {
  Map<String, Vocabulary> vocabularies;
  Map<String, Property> allprops;
  Map<String, Individual> allinds;
  Map<String, Type> alltypes;
  
  TripleUtil util;
  
  public TripleInput() {
    super();
    this.initialize();
  }

  public TripleInput(String mode) {
    super(mode);
    this.initialize();
  }
  
  protected void initialize() {
    this.vocabularies = new HashMap<String, Vocabulary>();
    this.allprops = new HashMap<String, Property>();
    this.allinds = new HashMap<String, Individual>();
    this.alltypes = new HashMap<String, Type>();
    
    this.util = new TripleUtil();
    this.setAutoCompletionHandler(this.completionHandler);
    this.addValueChangeHandler(this.changeHandler);
  }
  
  private List<Integer[]> getWordLocations(String triple) {
    List<Integer[]> locations = new ArrayList<Integer[]>();
    int start=0, end=0, i=0;
    boolean inword = false;
    for(i=0; i<triple.length(); i++) {
      char c = triple.charAt(i);
      if(c == ' ' && inword) {
        end = i;
        inword = false;
        locations.add(new Integer[]{start, end});
      }
      else if(c != ' ' && !inword) {
        start = i;
        inword = true;
      }
    }
    if(inword)
      locations.add(new Integer[]{start, i});
    
    return locations;
  }
  
  ValueChangeHandler<String> changeHandler = new ValueChangeHandler<String>() {
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
      validate();
    }
  };
  
  AutoCompletionHandler completionHandler = new AutoCompletionHandler() {
    @Override
    public void getCompletions(String text, EditorPosition caretPos,
        int caretIndex, AutoCompletionCallback callback) {

      String line = text.split("\n")[caretPos.getLineNumber()];
      String subline = line.substring(0, caretPos.getColumnNumber());
      
      List<Integer[]> locations = getWordLocations(line);
      int wordstart = 0, wordend = 0;
      int col = caretPos.getColumnNumber();
      int positionIndex = 0;
      for(Integer[] loc : locations) {
        positionIndex++;        
        if(col >= loc[0] && col <= loc[1]) {
          wordstart = loc[0];
          wordend = loc[1];
          break;
        }
      }
      String keyword = subline.substring(wordstart, col);
      
      EditorPosition fromPos = new EditorPosition(caretPos.getLineNumber(), wordstart);
      EditorPosition toPos = new EditorPosition(caretPos.getLineNumber(), wordend);

      List<String> suggestions = new ArrayList<String>();
      
      if(positionIndex == 1) {
        // Subject suggestions : All individuals       
        suggestions.addAll(allinds.keySet());
      }
      else if(positionIndex == 2) {
        // Property suggestions        
        suggestions.addAll(allprops.keySet());
        suggestions.add("a");
      }
      else if(positionIndex == 3) {
        // Object suggestions
        // -- Get all classes for property "a"
        String propstr = line.substring(locations.get(1)[0], locations.get(1)[1]);
        if(propstr.equals("a")) {
          suggestions.addAll(alltypes.keySet());
        }
        else {
          // -- TODO: Get predicate, find range, and get appropriate values        
          // -- For now just adding all individuals
          suggestions.addAll(allinds.keySet());
        }
      }
      Collections.sort(suggestions);
      
      List<AutoCompletionChoice> choices = new ArrayList<AutoCompletionChoice>();
      for(String suggestion : suggestions) {
        if(suggestion.startsWith(keyword))
          choices.add(new AutoCompletionChoice(suggestion, suggestion, 
              "cssName", fromPos, toPos));
      }
      AutoCompletionResult result = new AutoCompletionResult(choices, caretPos);
      callback.completionsReady(result);
    }
  };  
  
  public boolean validate() {
    this.clearErrorRange();
    boolean ok = true;
    
    // Validate triple items
    String[] lines = this.getValue().split("\\n");
    for(int i=0; i<lines.length; i++) {
      String line = lines[i];
      List<Integer[]> locations = this.getWordLocations(line);
      
      int start=0, end=0;
      if(locations.size() > 0) {
        String subject = line.substring(locations.get(0)[0], locations.get(0)[1]);
        start = 0;
        end = subject.length();
        if(subject.matches(".+:.+")) {
          if(!this.allinds.containsKey(subject)) {
            this.setErrorRange(new EditorPosition(i, start), 
                new EditorPosition(i, end));
            ok = false;
          }
        }
      }
        
      if(locations.size() > 1) {
        String predicate = line.substring(locations.get(1)[0], locations.get(1)[1]);
        start = end+1;
        end = start + predicate.length();
        if(predicate.matches(".+:.+")) {
          if(predicate.equals("a") || !this.allprops.containsKey(predicate)) {
            this.setErrorRange(new EditorPosition(i, start), 
                new EditorPosition(i, end));
            ok = false;
          }
        }
      }
      
      if(locations.size() > 2) {
        String object = line.substring(locations.get(2)[0], locations.get(2)[1]);
        start = end+1;
        end = start + object.length();
        if(object.matches("^\\w+:.+")) {
          if(!this.allinds.containsKey(object) && 
              !this.alltypes.containsKey(object)) {
            this.setErrorRange(new EditorPosition(i, start), 
                new EditorPosition(i, end));
            ok = false;
          }
        }
      }
    }
    return ok;
  }
  
  public List<Triple> getTriples() {
    List<Triple> triples = new ArrayList<Triple>();
    for(String tstr : this.getValue().split("\\n")) {
      Triple t = util.fromString(tstr);
      if(t != null)
        triples.add(t);
    }
    return triples;
  }
  
  public String getTripleString(List<Triple> triples) {
    String triplestr = "";
    boolean done = false;
    for(Triple t : triples) {
      if(done) 
        triplestr += "\n";
      triplestr += this.util.toString(t);
      done = true;
    }
    return triplestr;
  }
  
//  @Override
//  public void setValue(String value) {
//    super.setValue(value);
//    this.validate();
//  }
  
  public void setValue(List<Triple> triples) {
    this.setValue(this.getTripleString(triples));
  }
  
  public void setDefaultNamespace(String ns) {
    this.util.addNamespacePrefix("", ns);
  }
  
  public Vocabulary getVocabulary(String prefix) {
    return vocabularies.get(prefix);
  }
  
  public void loadVocabulary(final String prefix, final String uri) {
    DiskREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary result) {
        vocabularies.put(prefix, result);
        util.addNamespacePrefix(prefix, result.getNamespace());
        loadTerms(prefix, result);
      }      
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure("Could not load vocabulary for "+uri
            +" : " + reason.getMessage());
      }
    }, uri, false);
  }
  
  public void loadUserVocabulary(final String prefix, String userid, String domain) {
    DiskREST.getUserVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary result) {
        vocabularies.put(prefix, result);
        util.addNamespacePrefix(prefix, result.getNamespace());        
        loadTerms(prefix, result);
      }      
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure("Could not load user vocabulary"
            +" : " + reason.getMessage());
      }      
    }, userid, domain, false);
  }
  
  void loadTerms(String prefix, Vocabulary vocab) {
    for(Type type : vocab.getTypes().values())
      alltypes.put(prefix+":"+type.getName(), type);
    for(Individual ind : vocab.getIndividuals().values())
      allinds.put(prefix+":"+ind.getName(), ind);
    for(Property prop : vocab.getProperties().values())
      allprops.put(prefix+":"+prop.getName(), prop);    
  }
}