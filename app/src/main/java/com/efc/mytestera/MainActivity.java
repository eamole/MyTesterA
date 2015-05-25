package com.efc.mytestera;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


class Util{
    static Activity app;
    static void bind(Activity act) {
        app=act;
    }
    // a quick utility
    static void toast(String text){
        // this creates the View on the fly - for text only
        Toast toast = Toast.makeText(app, text, Toast.LENGTH_LONG)  ;//new Toast(app);
        // toast.setView(layout);   // not too sure what this LAYOUT is about
        // this is an internal Layout for the Toast
        // toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}
public class MainActivity extends Activity {

    Questions test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.bind(this);

        test=new MCQFixedOptions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // TODO
    // these are direct bindings with XML
    // do dynamic binding later!!
    // NOTE : MUST be public - else, not found!!
    public void onPrevClick(View v) {
        test.onPrevClick(v);
    }
    public void onNextClick(View v) {
        test.onNextClick(v);
    }
    public void onOptionClick(View v) {
        test.onOptionClick(v);
    }
    public void onOptionsClick(View v) {
        test.onOptionClicks(v);
    }
}

class MCQFixedOptions extends MCQFromFolderRaw {
    MCQFixedOptions(Activity app) {
        super("", app);
    }

}

// this is needed as a way for andoid to read files at runtime
class MCQFromFolderRaw extends Questions{
    MCQFromFolderRaw(String folder,Activity app) {
        // FIXME : app is passed to the inner class AND the outer class
        // this is a design issue because inner class has become static
        // need to review design!!
        super(new Questions.Reader.FromFolderRaw("<eamonn>",app) );
        // need a better constructor for Questions!!
        bind(app, R.id.layout_activity_main);
        configure(5, 250);
        init();
        // also need to supply a Questions.IReader
    }
}
/*
    handle the randomising of the orderr of the questions
    and the folder mapping
    create a list of filews from the folder - allow a max number of questions

    Defacto Controller - for a List
    TODO (only Iteraror used...needs really to be Navigable...)
 */
abstract class Questions {

    // app - see bindings below
    IReader reader;     // this is the source of the questions/Iterator
    int numQuestions;   // how many questions to ask

    int maxTime;        // for the timer

    // List<Question> questions;   // this should probably be an arrayList
    Iterator<Question> iterator;
    Question question;
    String[] answers;
    boolean[] isCorrect;

    // bindings
    Activity app;
    View layout;
    Button btnNext;
    Button btnPrev;

    /*
    static Questions FromFolder(String folder,String sep,Activity app) {
    }
    */

    Questions(IReader reader) {
        this.reader=reader;
    }

    void configure(int numQuestions,int maxTime) {
        this.numQuestions=numQuestions ;
        this.maxTime =maxTime ;
    }
    void init() {
        //Util.toast("Questions.init()");
        iterator = reader.init(numQuestions);          // bind the reader to this - use maxQuestions

        answers=new String[numQuestions];
        isCorrect=new boolean[numQuestions];
        //Util.toast("init() done");
        // should we limit
        // iterator=questions.listIterator();

    }
    // over
    void bind(Activity app, int layoutId) {
        bind(app,app.findViewById(layoutId));   // R.id.layout_activity_main
    }
    void bind(Activity app, View layout) {
        this.app=app;
        Resources res=app.getResources();
        this.layout=layout;

        // the action listeners are set in XML - only to the activity!!!
        btnNext=(Button) app.findViewById(R.id.btnNext);

        btnPrev=(Button) app.findViewById(R.id.btnPrev);
        btnPrev.setEnabled(false);
    }
    void onNextClick(View v) {
        // Util.toast("Questions.onNextClick");
        next();

        // FIXME - turning off too early
        if(!hasNext()) {
            btnNext.setEnabled(false);
        } else {
            btnNext.setEnabled(true);
        }
    }
    void onPrevClick(View v) {

        //TODO - make the list navigable rather that an iterator
        Util.toast("Questions.onPrevClick- not yet implemented");
    }
    void onOptionClick(View v) {
        // need to know which option has been selected
        Util.toast("Questions.onOptionClick");
    }
    void onOptionClicks(View v) {
        // need to know which option has been selected
        Util.toast("Questions.onOptionsClick");
    }
    void start() {
        next();
    }
    boolean hasNext() {
        return iterator.hasNext();
    }

    void next() {
        question=iterator.next();
        // now bind the question to the form
        question.bind(app);
        question.show();
    }

    /*
        TODO
        need to bind the Questions object to the Control of the Form to handle navigation thru the questions
        this is the ONLY requirements of a Reader???
        prob should be Navigable
    */
    interface IReader extends Iterator {

        public Iterator<Question> init(int numQuestions);   // request a number of questions, return the List

    }
    /*

        This is a DeFacto MVC Model
        This is the data source form the list/Iterator used in the controller above

     */
    static abstract class Reader implements IReader {

        /*
            This is an Android RAW file reader
            a driver
         */
        static class FileReader{
            Activity app;
            String packageName;
            Resources res;
            int resId;
            InputStream stream;
            FileReader(Activity app) {
                this.app=app;
            }
            FileReader open(String fileName) {
                packageName=app.getPackageName();
                res = app.getResources();
                resId = res.getIdentifier(fileName, "raw", packageName);
                stream=res.openRawResource(resId);
                return this;
            }
            String read() {
                String data=null;
                try{
                    byte[] buffer = new byte[stream.available()];
                    int bytes=stream.read(buffer,0,buffer.length);
                    data=new String(buffer,"UTF-8");

                } catch(IOException e) {}
                return data;

            }
            String get(String fileName) {
                return open(fileName).read();
            }
            void close() {

            }

        }
        // raw uses ID's
        static class FileReaderRaw{
            Activity app;
            String packageName;
            Resources res;
            // int resId;
            InputStream stream;
            FileReaderRaw(Activity app) {
                this.app=app;
                res=app.getResources();
            }
            FileReaderRaw open(int fileId) {
                /*packageName=app.getPackageName();
                res = app.getResources();
                resId = res.getIdentifier(fileName, "raw", packageName); */
                stream=res.openRawResource(fileId);
                return this;
            }
            String read() {
                String data=null;
                try{
                    byte[] buffer = new byte[stream.available()];
                    int bytes=stream.read(buffer,0,buffer.length);
                    data=new String(buffer,"UTF-8");

                } catch(IOException e) {}
                return data;

            }
            String get(int fileId) {
                return open(fileId).read();
            }
            void close() {

            }

        }

        /*

    This is the one that assumes the Question is stored as a string
    However it doesn't specify the source

    FIXME
    not too sure if this is at the right level - or wheteher this should be part of reader

 */
        public static abstract class FromString extends Reader {

            String sectionSeparator;

            FromString(String sectionSeparator) {

                this.sectionSeparator=sectionSeparator;

            }

        }


        /*


       */
        public static class FromFolder extends FromString  {
            Activity app;
            FileReader fileReader;
            Question.IReader reader;        // this is part of the Question object reader - bound to this
            String path="raw/";
            String[] fileNames;


            int current=-1; // first call give 0
            FromFolder(String path, String sectionSeparator, Activity app) {
                // this.reader=new QuestionReaderAsStrings(sectionSeparator);
                super(sectionSeparator);
                // this.path=path;
                this.app=app;
                this.fileReader = new FileReader(app);
                reader = new Question.Reader.FromFile(this,sectionSeparator) ;
            }
            public Iterator<Question> init(int numQuestions) {
                //Util.toast("FromFolder.init()");
                // need to read/count all the files in the folder

                // I think this is a Reflect.Field
                Field[] fields=R.raw.class.getFields();
                if(fields.length<=0) {
                    throw new Error("Error : no files in folder ["+"res/raw"+"]!!");
                }
                fileNames=new String[fields.length];
                for(int i=0;i<fields.length;i++) {
                    fileNames[i]=fields[i].getName();
                }

                return iterator();
            }
            /*

                this will not work for res/raw
                need to list assets in R.raw
                see above

             */
            public Iterator<Question> xxx_init(int numQuestions) {
                //Util.toast("FromFolder.init()");
                // need to read/count all the files in the folder
                File folder = new File(path);
                if(!folder.exists()){
                    throw new Error("Error : folder ["+path+"] does not exist");
                } else if(!folder.isDirectory())  {
                    throw new Error("Error : path exists ["+path+"] but not a folder");
                } else {
                    fileNames=folder.list();
                }
                if(fileNames.length<=0) {
                    throw new Error("Error : no files in folder ["+path+"]!!");
                }

                return iterator();
            }
            /*
                I/F IReader
            */
            public Iterator<Question>iterator() {
                return this;
            }
            /*
                I/F Iterator
             */
            public boolean hasNext() {
                return current<fileNames.length;
            }
            public Question next() {
                Util.toast("Iterator.next() "+current);
                current++;
                Question question=null;
                if (hasNext()) {
                    // File file=new File(path+fileNames[current]);
                    String str = fileReader.get(fileNames[current]);
                    // read the source
                    //Util.toast(str);
                    reader.setSource(str);
                    // extract the question from the rawSource
                    question = reader.getNextQuestion();


                }

                return question;
            }
            public void remove() {

            }
        }
        /*
            uses fileID's not fileNames
       */
        public static class FromFolderRaw extends FromString  {
            Activity app;
            FileReaderRaw fileReader;
            Question.IReader reader;        // this is part of the Question object reader - bound to this
            String path="raw/";
            int[] fileIds;
            //String[] fileNames; // TODO just to make it compile - delete!!


            int current=0;
            FromFolderRaw(String sectionSeparator, Activity app) {
                // this.reader=new QuestionReaderAsStrings(sectionSeparator);
                super(sectionSeparator);
                // this.path=path;
                this.app=app;
                this.fileReader = new FileReaderRaw(app);
                reader = new Question.Reader.FromFileRaw(this,sectionSeparator) ;
            }
            /*
                loop to collect ALL raw ID's

             */
            public Iterator<Question> init(int numQuestions) {

                fileIds = new int[3];
                fileIds[0]=R.raw.q1;
                fileIds[1]=R.raw.q2;
                fileIds[2]=R.raw.q3;

                return iterator();
            }
            public Iterator<Question> xxx2_init(int numQuestions) {
                //Util.toast("FromFolder.init()");
                // need to read/count all the files in the folder

                // I think this is a Reflect.Field
                Field[] fields=R.raw.class.getFields();
                if(fields.length<=0) {
                    throw new Error("Error : no files in folder ["+"res/raw"+"]!!");
                }
                fileIds=new int[fields.length];
                for(int i=0;i<fields.length;i++) {
                    //fileIds[i]=fields[i].getInt();
                }

                return iterator();
            }
            /*

                this will not work for res/raw
                need to list assets in R.raw
                see above

             */
            public Iterator<Question> xxx_init(int numQuestions) {
                //Util.toast("FromFolder.init()");
                // need to read/count all the files in the folder
                File folder = new File(path);
                if(!folder.exists()){
                    throw new Error("Error : folder ["+path+"] does not exist");
                } else if(!folder.isDirectory())  {
                    throw new Error("Error : path exists ["+path+"] but not a folder");
                } else {
                    // fileNames=folder.list();
                }
                if(fileIds.length<=0) {
                    throw new Error("Error : no files in folder ["+path+"]!!");
                }

                return iterator();
            }
            /*
                I/F IReader
            */
            public Iterator<Question>iterator() {
                return this;
            }
            /*
                I/F Iterator
             */
            public boolean hasNext() {
                return current<fileIds.length;
            }
            public Question next() {
                Util.toast("Iterator.next() - "+current);
                current++;
                Question question=null;
                if (hasNext()) {
                    // File file=new File(path+fileNames[current]);
                    String str = fileReader.get(fileIds[current]);
                    // read the source
                    //Util.toast(str);
                    reader.setSource(str);
                    // extract the question from the rawSource
                    question = reader.getNextQuestion();


                }

                return question;
            }
            public void remove() {

            }
        }

    }


}



/*

    The question holder
    this should be a cross between a generic question
    and a special exact structure!!!

    this is bound to IQuestionReader - which is specific

    this is a reused singleton bind once!!!

 */
class Question {
    static final String letters="ABCDEFGHIJK";
    Questions test;

    Activity app;
    Resources res;

    // this should really be an extension and specified by an intereface
    // for this specific structure
    String code;
    String question;
    //HashMap<String,String> options = new HashMap<String,String>();
    String[] options=new String[4];
    String answer;      // this is the key of the hashmap - may use 1..9 or A..Z
    String solution;
    String explanation;
    boolean answered=false;

    HashMap<String,View> controls = new HashMap<String,View>();
    TextView txtCode;
    TextView txtQuestion;
    TextView txtAnswer;
    TextView txtSolution;
    TextView txtExplanation;

    RadioGroup rbOptions;
    RadioButton rbOption1;
    RadioButton rbOption2;
    RadioButton rbOption3;
    RadioButton rbOption4;




    boolean useLetters=true;

    boolean isCorrect() {
        return isAnswered() && (answer.equals(solution));
    }
    boolean isAnswered() {

        return answer.length()>0;

    }
    /*

        this is too tightly bound to the question structure
        it should be a message like getAll and getAll

     */
    static Question getNextQuestion(IReader source) {
        Question q=new Question();
        q.code=source.getCode();
        q.question = source.getQuestion() ;

        String[] options = source.getOptions() ;
        for(int i=0;i<4;i++) {
            q.options[i]="" + letters.charAt(i)+". "+options[i];  // INC NBB
            // q.options.put("" + letters.charAt(i), option);
        }

        q.answer = source.getAnswer().trim() ;
        // q.explanation  = source.getExplanation() ;

        return q;
    }
    /*
        this is too tightly coupled with a specific layout
     */
    public void bind(Activity app) {
        this.app=app;
        res=app.getResources();
        controls.put("code",app.findViewById(R.id.txtCode));
        controls.put("question",app.findViewById(R.id.txtQuestion));
        controls.put("answer",app.findViewById(R.id.txtAnswer));
        controls.put("solution",app.findViewById(R.id.txtSolution));
        controls.put("explanation",app.findViewById(R.id.txtExplanation));

        controls.put("option1",app.findViewById(R.id.rbOption1 ));
        controls.put("option2",app.findViewById(R.id.rbOption2 ));
        controls.put("option3",app.findViewById(R.id.rbOption3 ));
        controls.put("option4",app.findViewById(R.id.rbOption4 ));

        txtCode = (TextView) app.findViewById(R.id.txtCode);
        txtQuestion = (TextView) app.findViewById(R.id.txtQuestion);
        txtAnswer = (TextView) app.findViewById(R.id.txtAnswer);
        txtSolution = (TextView) app.findViewById(R.id.txtSolution);
        txtExplanation = (TextView) app.findViewById(R.id.txtExplanation);

        rbOption1=(RadioButton) app.findViewById(R.id.rbOption1 );
        rbOption2=(RadioButton) app.findViewById(R.id.rbOption2 );
        rbOption3=(RadioButton) app.findViewById(R.id.rbOption3 );
        rbOption4=(RadioButton) app.findViewById(R.id.rbOption4 );


    }
    void show() {

        txtCode.setText(code);
        txtQuestion.setText(question);
        rbOption1.setText(options[0]);
        rbOption2.setText(options[1]);
        rbOption3.setText(options[2]);
        rbOption4.setText(options[3]);
        txtAnswer.setText(answer);
        txtSolution.setText(solution);
        txtExplanation.setText(explanation);


    }
    // no bindings yet
    void onClick(View v) {
        Util.toast("Question.onClick");

    }

    /*
        this may define the structure of the question AND the reader/parser

        this is tightly coupled with these types of questions - there needs to be a binding to
        the UI and the Question structure!!
        TODO
        actually - this is the shared Interface which is the structure of
        a Specific Questionaire or perhaps a type - this should be survey specific -
        but shareable - it is not generic enough
        the base interface should be here -
    */
    interface IReader {

        static final int CODE=0;
        static final int QUESTION=1;
        static final int OPTIONS=2;
        static final int ANSWER=3;
        static final int EXPLANATION=4;

        // the system requires that this method split the string into parts as dictated by sectionSeparator
        void setSource(String source);  // this source is the id etc
        Question getNextQuestion(); // this is really a factory method

        /*
            this is the bit that should be outside
            this should be a separate i/f, extending Question.IReader
            which defines the interface to the file/structure
            useful also to share with View

            the question interface - the structure of the question - should model
            the Question holder as well
           */
        String getCode();
        String getQuestion();
        String[] getOptions();
        String getAnswer();
        String getExplanation();

    }


    static abstract class Reader implements IReader {
        Questions.IReader listReader; // this is a back link to the list reader
        Reader(Questions.IReader listReader) {
            this.listReader=listReader;
        }

        /*
            an implementation specific Question Parser
            a singleton is sufficient - can't call statics dynamically
            needs a method per stored property

            this may be inside the QuestionsString!!!
            therefore the Questions implementation (QuestionsString

            Might be able to implement this properly as a static!!
            rather than as a singleton factory!!!
            Except no interface for statics


            This is a reusable singleton/factory - bound to the listReader

        */
        static class FromString extends Reader implements IReader{
            // FIXME android specific linebreak???
            static final String lineBreak = "\n";

            String sectionSeparator; // this may be universal!! therefore static - no constructor
            String rawData;

            String[] parts;

            FromString(Questions.IReader listReader,String sectionSeparator) {
                super(listReader);
                this.sectionSeparator=sectionSeparator;
                //this.rawData=rawData;
            }

            public Question getNextQuestion() {
                // FIXME messy - this is how the Question is populated --- from Strings!!
                return Question.getNextQuestion(this);
            }

            public void setSource(String rawData) {
                this.rawData=rawData;
                parts=rawData.split(sectionSeparator);
                // this should create 4 parts

            }
            public String getCode() {
                return parts[CODE];
            }
            public String getQuestion() {
                return parts[QUESTION];
            }
            public String[] getOptions() {
                //Util.toast(parts[OPTIONS]);
                // trim is important to eliminate blank lines
                String[] options = parts[OPTIONS].trim().split(lineBreak);
                //Util.toast(options[0]);
                return options;
            }
            public String getAnswer() {
                return parts[ANSWER];
            }
            public String getExplanation() {
                return parts[EXPLANATION];
            }


        }
        /*
            this is a reusable singleton
            it is a Question factory
         */
        static class FromFile extends FromString{

            Questions.Reader.FileReader fileReader;
            // String fileName;    // transient - shouldn't store
            // the filereader is constructed externally - only
            FromFile(Questions.Reader.FromFolder listReader,String sectionSeparator) {
                super(listReader,sectionSeparator);
                this.fileReader=listReader.fileReader;
            }
            @Override
            public void setSource(String fileName) {
                super.setSource(fileReader.get(fileName));
            }
        }
        static class FromFileRaw extends FromString{

            Questions.Reader.FileReaderRaw fileReader;
            // String fileName;    // transient - shouldn't store
            // the filereader is constructed externally - only
            FromFileRaw(Questions.Reader.FromFolderRaw listReader,String sectionSeparator) {
                super(listReader,sectionSeparator);
                this.fileReader=listReader.fileReader;
            }
            // @Override - changed to overload - using ID
            public void setSource(int fileId) {
                super.setSource(fileReader.get(fileId));
            }
        }

    }



}





