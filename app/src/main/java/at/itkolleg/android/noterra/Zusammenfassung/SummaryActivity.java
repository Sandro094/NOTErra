package at.itkolleg.android.noterra.Zusammenfassung;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.*;
import at.itkolleg.android.noterra.DatenbankSQLite.DBHandler;
import at.itkolleg.android.noterra.Synchronisation.FTPHandler;
import at.itkolleg.android.noterra.Synchronisation.HTTPHandler;
import at.itkolleg.android.noterra.MainActivity;
import at.itkolleg.android.noterra.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Diese Klasse gibt nochmals alle aufgenommenen Daten der Datenbank zurück und zeigt Bild , Audio und Textnoitz.
 *
 * @author Gutsche Christoph, Berger Sandro
 */
public class SummaryActivity extends ActionBarActivity {

    private String outputFile = null;
    private ImageView imageView;
    private ArrayList<String> imagePath = new ArrayList<>();
    private ArrayList<String> audioPath = new ArrayList<>();
    private DBHandler forstdb;

    private String audiofpad;
    private MediaPlayer myplayer;
    private Chronometer chronmeter;
    private SeekBar seek;

    private TextView dur;
    private TextView pos;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    public static int oneTimeOnly = 0;


    //DB Tiel - Notizen
    private TextView test;
    private TextView notetext;

    //Haupt -Formular
    private TextView gemeinde;
    private TextView kostenschaetzung;
    private TextView empfmaßnahmen;
    private TextView prioritaet;
    private TextView foerderfaehig;
    private TextView abwicklung;
    private TextView defmaßnahmen;
    private TextView defmaßnahmenjanein;
    private String idHauptform;

    //Spezielles Fromular
    private String idAbflussbehinderndeeinbauten = " ";
    private String idAbflussbehinderung = " ";
    private String idAblagerung = " ";
    private String idHolzablagerung = " ";
    private String idHolzbewuchs = " ";
    private String idSchaedenRegulierungsbauten = " ";
    private String idWasserazseinleitung = " ";

    private TextView h1; //Überschrift Formular
    private TextView sf1tv;
    private TextView sf2tv;
    private TextView sf3tv;
    private TextView sf4tv;
    private TextView sf5tv;
    private TextView sf6tv;


    /**
     * Diese Methode wird Automatisch beim Klassenaufruf gestartet.
     * Initialisierung der Activity und Appstart.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));

        initialisierung();

        datenbankVerbindung();

        loadImage();

        audioinitialisierung();

        loadText();

        loadFormular();

        if (forstdb.tableexist("tbl_Abflussbehinderung") != 0) {
            loadAbflussbehinderung();
        }
        if (forstdb.tableexist("tbl_OhneBehinderung") != 0) {
            loadOhneBehinderung();
        }
        if (forstdb.tableexist("tbl_Ablagerung") != 0) {

            loadAblagerung();
        }
        if (forstdb.tableexist("tbl_Holzablagerung") != 0) {
            loadHolzablagerung();
        }
        if (forstdb.tableexist("tbl_Holzbewuchs") != 0) {
            loadHolzbewuchs();
        }
        if (forstdb.tableexist("tbl_SchadenAnRegulierung") != 0) {
            loadSchadenAnRegulierung();
        }
        if (forstdb.tableexist("tbl_WasserAusEinleitung") != 0) {
            loadWasserAusEinleitung();
        }
    }

    /**
     * Diese Methode erstellt einen neuen DB Hanlder der für die Interaktion der DB erstellt wird.
     */
    private void datenbankVerbindung() {
        forstdb = new DBHandler(this);
    }

    /**
     * Diese Methode dient zur initialisierung der Textfelder (TextvView's), Buttons und der Seekbar.
     */
    private void initialisierung() {
        dur = (TextView) findViewById(R.id.dur);
        pos = (TextView) findViewById(R.id.pos);

        chronmeter = (Chronometer) findViewById(R.id.chronometer);
        seek = (SeekBar) findViewById(R.id.seekBar);

        notetext = (TextView) findViewById(R.id.notetext);


        //HauptFormular
        gemeinde = (TextView) findViewById(R.id.gemeinde);
        kostenschaetzung = (TextView) findViewById(R.id.kostenschaetzung);
        empfmaßnahmen = (TextView) findViewById(R.id.empfmaßnhamen);
        prioritaet = (TextView) findViewById(R.id.prioritaet);
        foerderfaehig = (TextView) findViewById(R.id.foerderfaehig);
        abwicklung = (TextView) findViewById(R.id.abwicklung);
        defmaßnahmen = (TextView) findViewById(R.id.defmaßnahmen);
        defmaßnahmenjanein = (TextView) findViewById(R.id.defmaßnahmenjanein);

        //Spezielles Formular
        h1 = (TextView) findViewById(R.id.sfh);
        sf1tv = (TextView) findViewById(R.id.sf1);
        sf2tv = (TextView) findViewById(R.id.sf2);
        sf3tv = (TextView) findViewById(R.id.sf3);
        sf4tv = (TextView) findViewById(R.id.sf4);
        sf5tv = (TextView) findViewById(R.id.sf5);
        sf6tv = (TextView) findViewById(R.id.sf6);
    }

    /**
     * Diese Methode erstellt den Medienplayer und falls ein Audiofile vorhanden ist wird dieses in den Medianplayer eingelesen.
     */
    private void audioinitialisierung() {

        ArrayList<String> audioRefArray = forstdb.getRefFromAudioTable();

        if(audioRefArray.size()>=1){
            audiofpad = audioRefArray.get(audioRefArray.size() - 1);
        }

        if (audiofpad != null) {
            myplayer = new MediaPlayer();
            try {
                FileInputStream audioFile = new FileInputStream(audiofpad);
                myplayer.setDataSource(audioFile.getFD());
                myplayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Diese Methode setzt das Notiztextfeld mit der Notiz aus dem Notesfeld.
     */
    public void loadText() {
        if (forstdb.tableexist("tbl_text") != 0) {
            //Notizen-Text
            Cursor c = forstdb.getLastInformation("Text", "tbl_Text");
            String text1 = c.getString(0);
            notetext.setText(text1);
        }
    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem Hauptformular.
     */
    public void loadFormular() {

        if (forstdb.tableexist("tbl_Formular") != 0) {
            //Hauptformular
            //Gemeinde
            Cursor cgemeinde = forstdb.getLastInformation("Gemeinde", "tbl_Formular");
            String gemeindetext = cgemeinde.getString(0);
            gemeinde.setText("Gemeinde: " + gemeindetext);


            //Kostenschätzung
            Cursor ckosten = forstdb.getLastInformation("Kosten", "tbl_Formular");
            String ckostentext = ckosten.getString(0);
            kostenschaetzung.setText("Kostenschätzung: " + ckostentext + " Euro");

            //Empfohlene Maßnahme
            Cursor cempfmaß = forstdb.getLastInformation("Massnahme", "tbl_Formular");
            String cempfmaßtext = cempfmaß.getString(0);
            empfmaßnahmen.setText("Empfohlene Maßnahme: " + cempfmaßtext);

            //Priorität
            Cursor cprioritaet = forstdb.getLastInformation("Prioritaet", "tbl_Formular");
            String cprioritaettext = cprioritaet.getString(0);
            prioritaet.setText("Priorität: " + cprioritaettext);

            //Förderfähig
            Cursor cfoederfaehig = forstdb.getLastInformation("Foerderfaehig", "tbl_Formular");
            String cfoederfaehigtext = cfoederfaehig.getString(0);

            if (cfoederfaehigtext.equals("0")) {
                cfoederfaehigtext = "Nein";
            } else {
                cfoederfaehigtext = "Ja";
            }
            foerderfaehig.setText("Förderfähig: " + cfoederfaehigtext);


            //Abwicklung
            Cursor cabwicklung = forstdb.getLastInformation("Abwicklung", "tbl_Formular");
            String cabwicklungtext = cabwicklung.getString(0);
            abwicklung.setText("Abwicklung: " + cabwicklungtext);

            //Definierte Maßnahmen

            //Absturzsicherung
            Cursor cabsturzsicherung = forstdb.getLastInformation("Absturzsicherung", "tbl_Formular");
            String cabsturzsicherungtext = cabsturzsicherung.getString(0);

            if (cabsturzsicherungtext.equals("0")) {
                cabsturzsicherungtext = "Nein";
            } else {
                cabsturzsicherungtext = "Ja";
            }

            //Bäume Sträuche Fällen
            Cursor cbaumfallen = forstdb.getLastInformation("BaumFaellen", "tbl_Formular");
            String cbaumfallentext = cbaumfallen.getString(0);

            if (cbaumfallentext.equals("0")) {
                cbaumfallentext = "Nein";
            } else {
                cbaumfallentext = "Ja";
            }


            //BauwerkSanieren
            Cursor cbauwerksan = forstdb.getLastInformation("BauwerkSanieren", "tbl_Formular");
            String cbauwerksantext = cbauwerksan.getString(0);

            if (cbauwerksantext.equals("0")) {
                cbauwerksantext = "Nein";
            } else {
                cbauwerksantext = "Ja";
            }


            //Bauwerk warten
            Cursor cbauwerkwart = forstdb.getLastInformation("BauwerkWarten", "tbl_Formular");
            String cbauwerkwarttext = cbauwerkwart.getString(0);

            if (cbauwerkwarttext.equals("0")) {
                cbauwerkwarttext = "Nein";
            } else {
                cbauwerkwarttext = "Ja";
            }


            //Durchlass Freilegen
            Cursor cdurchlassfrei = forstdb.getLastInformation("DurchlassFreilegen", "tbl_Formular");
            String cdurchlassfreitext = cdurchlassfrei.getString(0);

            if (cdurchlassfreitext.equals("0")) {
                cdurchlassfreitext = "Nein";
            } else {
                cdurchlassfreitext = "Ja";
            }


            //Genehmigung Pruefen
            Cursor cgenehmigpruf = forstdb.getLastInformation("GenemigungPruefen", "tbl_Formular");
            String cgenehmigpruftext = cgenehmigpruf.getString(0);

            if (cgenehmigpruftext.equals("0")) {
                cgenehmigpruftext = "Nein";
            } else {
                cgenehmigpruftext = "Ja";
            }


            //Hindernisse Entfernen
            Cursor chindernentfernen = forstdb.getLastInformation("HindernisseEntfernen", "tbl_Formular");
            String chindernentfernentext = chindernentfernen.getString(0);

            if (chindernentfernentext.equals("0")) {
                chindernentfernentext = "Nein";
            } else {
                chindernentfernentext = "Ja";
            }


            //Hinderniss Sprengen
            Cursor chinderspreng = forstdb.getLastInformation("HindernissSprengen", "tbl_Formular");
            String chindersprengtext = chinderspreng.getString(0);

            if (chindersprengtext.equals("0")) {
                chindersprengtext = "Nein";
            } else {
                chindersprengtext = "Ja";
            }


            //Holz Ablängen
            Cursor cholzablng = forstdb.getLastInformation("HolzAblaengen", "tbl_Formular");
            String cholzablngtext = cholzablng.getString(0);

            if (cholzablngtext.equals("0")) {
                cholzablngtext = "Nein";
            } else {
                cholzablngtext = "Ja";
            }


            //Keine Maßnhamen
            Cursor ckeinemaßn = forstdb.getLastInformation("KeineMassnahme", "tbl_Formular");
            String ckeinemaßntext = ckeinemaßn.getString(0);

            if (ckeinemaßntext.equals("0")) {
                ckeinemaßntext = "Nein";
            } else {
                ckeinemaßntext = "Ja";
            }


            //Sperre oder Gerinne räumen
            Cursor cspodgerinner = forstdb.getLastInformation("SperreOdGerinneRaumen", "tbl_Formular");
            String cspodgerinnertext = cspodgerinner.getString(0);

            if (cspodgerinnertext.equals("0")) {
                cspodgerinnertext = "Nein";
            } else {
                cspodgerinnertext = "Ja";
            }


            //Ufer Sichern
            Cursor cufersich = forstdb.getLastInformation("UferSichern", "tbl_Formular");
            String cufersichtext = cufersich.getString(0);

            if (cufersichtext.equals("0")) {
                cufersichtext = "Nein";
            } else {
                cufersichtext = "Ja";
            }


            //Zustand beobachten
            Cursor czustandbeob = forstdb.getLastInformation("ZustandBeobachten", "tbl_Formular");
            String czustandbeobtext = czustandbeob.getString(0);

            if (czustandbeobtext.equals("0")) {
                czustandbeobtext = "Nein";
            } else {
                czustandbeobtext = "Ja";
            }


            defmaßnahmen.setText("\n Definierte Maßnahmen " +
                    "\n Absturzsicherung: " +
                    "\n Bäume / Sträucher fällen: " +
                    "\n Bauwerk sanieren: " +
                    "\n Bauwerk warten: " +
                    "\n Durchlass freigen: " +
                    "\n Genehmigung prüfen: " +
                    "\n Hindernisse entfernen / räumen: " +
                    "\n Hinderniss sprengen: " +
                    "\n Holz ablängen: " +
                    "\n Keine Maßnahmen: " +
                    "\n Sperre oder Gerinne räumen: " +
                    "\n Ufer sichern: " +
                    "\n Zustand beobachten: ");

            defmaßnahmenjanein.setText("Checkliste " +
                    "\n" + cabsturzsicherungtext +
                    "\n" + cbaumfallentext +
                    "\n" + cbauwerksantext +
                    "\n" + cbauwerkwarttext +
                    "\n" + cdurchlassfreitext +
                    "\n" + cgenehmigpruftext +
                    "\n" + chindernentfernentext +
                    "\n" + chindersprengtext +
                    "\n" + cholzablngtext +
                    "\n" + ckeinemaßntext +
                    "\n" + cspodgerinnertext +
                    "\n" + cufersichtext +
                    "\n" + czustandbeobtext);

            idHauptform = forstdb.getIDfromTable("tbl_Formular", "idFormular");
        }
    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular Abflussbehinderndeeinbauten.
     */
    public void loadAbflussbehinderung() {
        idAbflussbehinderndeeinbauten = forstdb.getIDfromTable("tbl_Abflussbehinderung", "idAbflussbehinderung");

        if (idHauptform.equals(idAbflussbehinderndeeinbauten)) {
            h1.setText("Abflussbehindernde Einbauten");

            Cursor carteinbaut = forstdb.getLastInformation("Art", "tbl_Abflussbehinderung");
            String carteinbauttext = carteinbaut.getString(0);
            sf1tv.setText("\nArt der Einbauten: " + carteinbauttext);


            Cursor cbeschreibung = forstdb.getLastInformation("Beschreibung", "tbl_Abflussbehinderung");
            String cbeschreibungtext = cbeschreibung.getString(0);
            sf2tv.setText("Beschreibung: " + cbeschreibungtext);

        }
    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Fromular Abflussbehinderung.
     */
    public void loadOhneBehinderung() {
        idAbflussbehinderung = forstdb.getIDfromTable("tbl_OhneBehinderung", "idOhneBehinderung");


        if (idHauptform.equals(idAbflussbehinderung)) {
            h1.setText("Ereignis ohne unmittelbare Abflussbehinderung");

            Cursor cartbeobachtung = forstdb.getLastInformation("Art", "idOhneBehinderung");
            String cartbeobachtungtext = cartbeobachtung.getString(0);
            sf1tv.setText("\nArt der Beobachtung: " + cartbeobachtungtext);


            Cursor cbeschreibung = forstdb.getLastInformation("Beschreibung", "idOhneBehinderung");
            String cbeschreibungtext = cbeschreibung.getString(0);
            sf2tv.setText("Beschreibung: " + cbeschreibungtext);

        }

    }


    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular Ablagerung.
     */
    public void loadAblagerung() {
        idAblagerung = forstdb.getIDfromTable("tbl_Ablagerung", "idAblagerung");

        if (idHauptform.equals(idAblagerung)) {
            h1.setText("Ablagerung sonstiger abflusshemender Gegenstände");

            Cursor cablagerung = forstdb.getLastInformation("Art", "tbl_Ablagerung");
            String cablagerungtext = cablagerung.getString(0);
            sf1tv.setText("\nAblagerung: " + cablagerungtext);


            Cursor cbeschreibung = forstdb.getLastInformation("Beschreibung", "tbl_Ablagerung");
            String cbeschreibungtext = cbeschreibung.getString(0);
            sf2tv.setText("Beschreibung: " + cbeschreibungtext);

            Cursor cbachabschnitt = forstdb.getLastInformation("Bachabschnitt", "tbl_Ablagerung");
            String cbachabschnitttext = cbachabschnitt.getString(0);
            sf3tv.setText("Länge des Bachabschnittes: " + cbachabschnitttext + " meter");

            Cursor causmass = forstdb.getLastInformation("Ausmass", "tbl_Ablagerung");
            String causmasstext = causmass.getString(0);
            sf4tv.setText("Größe / Ausmaß: " + causmasstext + "m2");

        }

    }


    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular Holzablagerung.
     */
    public void loadHolzablagerung() {
        idHolzablagerung = forstdb.getIDfromTable("tbl_Holzablagerung", "idHolzablagerung");
        if (idHauptform.equals(idHolzablagerung)) {
            h1.setText("Holzablagerungen im Hochwasserabflussbereich");

            Cursor canzhalstamm = forstdb.getLastInformation("AnzahlStaemme", "tbl_Holzablagerung");
            Integer canzhalstammint = canzhalstamm.getInt(0);
            sf1tv.setText("\nAnzahl der Stämme: " + canzhalstammint);

            Cursor cbaumart = forstdb.getLastInformation("Baumart", "tbl_Holzablagerung");
            String cbaumarttext = cbaumart.getString(0);
            sf2tv.setText("Baumart: " + cbaumarttext);

            Cursor cmedia = forstdb.getLastInformation("Media", "tbl_Holzablagerung");
            Integer cmediaint = cmedia.getInt(0);
            sf3tv.setText("Media (BHD): " + cmediaint);

            Cursor cholzmenge = forstdb.getLastInformation("Holzmenge", "tbl_Holzablagerung");
            Integer cholzmengeint = cholzmenge.getInt(0);
            sf4tv.setText("Anzahl der Stämme: " + cholzmengeint);

            Cursor cbachabschnitt = forstdb.getLastInformation("Bachabschnitt", "tbl_Holzablagerung");
            Integer cbachabschnittint = cbachabschnitt.getInt(0);
            sf5tv.setText("Länge Bachabschnitt: " + cbachabschnittint + " meter");

        }
    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular Holzbewuchs.
     */
    public void loadHolzbewuchs() {
        idHolzbewuchs = forstdb.getIDfromTable("tbl_Holzbewuchs", "idHolzbewuchs");

        if (idHauptform.equals(idHolzbewuchs)) {
            h1.setText("Holzbewuchs im Hochwasserabflussbereich");


            Cursor canzahl = forstdb.getLastInformation("Anzahl", "tbl_Holzbewuchs");
            Integer canzahlint = canzahl.getInt(0);
            sf1tv.setText("\nAnzahl der Stämme / Sträucher: " + canzahlint);

            Cursor cbaumart = forstdb.getLastInformation("Baumart", "tbl_Holzbewuchs");
            String cbaumarttext = cbaumart.getString(0);
            sf2tv.setText("Baumart: " + cbaumarttext);

            Cursor choehe = forstdb.getLastInformation("Hoehe", "tbl_Holzbewuchs");
            Integer choeheint = choehe.getInt(0);
            sf3tv.setText("Baumhöhe: " + choeheint + " meter");

            Cursor cholzmenge = forstdb.getLastInformation("Menge", "tbl_Holzbewuchs");
            Integer cholzmengeint = cholzmenge.getInt(0);
            sf4tv.setText("Holzmenge: " + cholzmengeint + " fm");

            Cursor cbeschreibung = forstdb.getLastInformation("Beschreibung", "tbl_Holzbewuchs");
            String cbeschreibungtext = cbeschreibung.getString(0);
            sf5tv.setText("Beschreibung: " + cbeschreibungtext);
        }
    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular SchadenAnRegulierung.
     */
    public void loadSchadenAnRegulierung() {
        idSchaedenRegulierungsbauten = forstdb.getIDfromTable("tbl_SchadenAnRegulierung", "idSchadenAnRegulierung");
        if (idHauptform.equals(idSchaedenRegulierungsbauten)) {
            h1.setText("Schäden an Regulierungsbauten");

            Cursor cart = forstdb.getLastInformation("Art", "tbl_SchadenAnRegulierung");
            String carttext = cart.getString(0);
            sf1tv.setText("\nArt des Bauwerks: " + carttext);

            Cursor choehe = forstdb.getLastInformation("Hoehe", "tbl_SchadenAnRegulierung");
            String choehetext = choehe.getString(0);
            sf2tv.setText("Freie Höhe bis Dammkrone: " + choehetext + " meter");


            Cursor cfehlabsturz = forstdb.getLastInformation("FehlendeAbsturzsicherung", "tbl_SchadenAnRegulierung");
            String cfehlabsturztext = cfehlabsturz.getString(0);

            if (cfehlabsturztext.equals("0")) {
                cfehlabsturztext = "Nein";
            } else {
                cfehlabsturztext = "Ja";
            }

            Cursor caussperre = forstdb.getLastInformation("AusgangSperrenfluegel", "tbl_SchadenAnRegulierung");
            String caussperretext = caussperre.getString(0);

            if (caussperretext.equals("0")) {
                caussperretext = "Nein";
            } else {
                caussperretext = "Ja";
            }

            Cursor csperre = forstdb.getLastInformation("Geschiebesperre", "tbl_SchadenAnRegulierung");
            String csperretext = csperre.getString(0);

            if (csperretext.equals("0")) {
                csperretext = "Nein";
            } else {
                csperretext = "Ja";
            }

            Cursor crisse = forstdb.getLastInformation("Risse", "tbl_SchadenAnRegulierung");
            String crissetext = crisse.getString(0);

            if (crissetext.equals("0")) {
                crissetext = "Nein";
            } else {
                crissetext = "Ja";
            }

            Cursor cmauerwerk = forstdb.getLastInformation("SchadhaftesMauerwerk", "tbl_SchadenAnRegulierung");
            String cmauerwerktext = cmauerwerk.getString(0);

            if (cmauerwerktext.equals("0")) {
                cmauerwerktext = "Nein";
            } else {
                cmauerwerktext = "Ja";
            }

            Cursor csonstiges = forstdb.getLastInformation("Sonstiges", "tbl_SchadenAnRegulierung");
            String csonstigestext = csonstiges.getString(0);

            if (csonstigestext.equals("0")) {
                csonstigestext = "Nein";
            } else {
                csonstigestext = "Ja";
            }

            Cursor cbewuchs = forstdb.getLastInformation("Bewuchs", "tbl_SchadenAnRegulierung");
            String cbewuchstext = cbewuchs.getString(0);

            if (cbewuchstext.equals("0")) {
                cbewuchstext = "Nein";
            } else {
                cbewuchstext = "Ja";
            }

            Cursor cunfun = forstdb.getLastInformation("UnterspueltesFundament", "tbl_SchadenAnRegulierung");
            String cunfuntext = cunfun.getString(0);

            if (cunfuntext.equals("0")) {
                cunfuntext = "Nein";
            } else {
                cunfuntext = "Ja";
            }


            sf5tv.setText("\n Schadensart des Bauwerks" +
                            "\n Fehlende Absturzsicherung " +
                            "\n Ausgang Sperrenflügel: " +
                            "\n Geschiebesperre: " +
                            "\n Risse im Mauerwerk / Beton: " +
                            "\n Schadhaftes Mauerwerk / Beton: " +
                            "\n Sonstiges" +
                            "\n Starker Bewuchs: " +
                            "\n Untersp. Fundament: "
            );


            sf6tv.setText("Checkliste " +
                            "\n" + cfehlabsturztext +
                            "\n" + caussperretext +
                            "\n" + csperretext +
                            "\n" + crissetext +
                            "\n" + cmauerwerktext +
                            "\n" + csonstigestext +
                            "\n" + cbewuchstext +
                            "\n" + cunfuntext
            );

        }


    }

    /**
     * Diese Methode setzt das jeweilige Textfeld mit den Daten aus dem speziellen Formular Wasser Aus- Einleitung.
     */
    public void loadWasserAusEinleitung() {
        if (forstdb.tableexist("tbl_WasserAusEinleitung") != 0) {
            idWasserazseinleitung = forstdb.getIDfromTable("tbl_WasserAusEinleitung", "idWasserAusEinleitung");

            if (idHauptform.equals(idWasserazseinleitung)) {
                h1.setText("Wasseraus-und -einleitungen");

                Cursor cart = forstdb.getLastInformation("Art", "tbl_WasserAusEinleitung");
                String carttext = cart.getString(0);
                sf1tv.setText("Art: " + carttext);

                Cursor czweck = forstdb.getLastInformation("Zweck", "tbl_WasserAusEinleitung");
                String czwecktext = czweck.getString(0);
                sf2tv.setText("Zweck: " + czwecktext);

                Cursor cbeschreibung = forstdb.getLastInformation("Beschreibung", "tbl_WasserAusEinleitung");
                String cbeschreibungtext = cbeschreibung.getString(0);
                sf3tv.setText("Beschreibung: " + cbeschreibungtext);
            }
        }
    }

    /**
     * Diese Methode ladet das Bild aus der Datenbank und gibt es dann auf den Bildschirm zurück.
     */
    public void loadImage() {
        ArrayList<String> RefArray = forstdb.getRefFromImageTable();
        File imgFile = null;

        if(RefArray.size()>=1){
            outputFile = RefArray.get(RefArray.size() - 1);
        }


        if (!(outputFile == null)) {
            imgFile = new File(outputFile);


            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView = (ImageView) this.findViewById(R.id.imageview);
                imageView.setImageBitmap(myBitmap);
            }
        }
    }

    /**
     * Diese Runnable wird benützt um die Zeit der Sprachmemo zu synchroniseren. Zusätzlich kann man auch die Seekbar mithilfe dieser Runnable verschieben und bearbeiten.
     */
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = myplayer.getCurrentPosition();
            dur.setText(String.format("%d min, %d sec",

                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seek.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);

            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        myplayer.pause();
                        myplayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                }
            });


        }
    };


    /**
     * Liest die sowohl den Bild als auch die Audiopfad aus der Datenbank aus
     * und weist diese den zwei dafür vorgesehenen Path-Variablen zu.
     *
     * @author Berger Sandro
     */
    public void loadData() {
        imagePath = forstdb.getRefFromImageTable();
        audioPath = forstdb.getRefFromAudioTable();
    }

    /**
     * Die Method liefer alle Bild-Referenzen die sich in der
     * tbl_Foto befinden.
     *
     * @return ArrayList of String (Bild-Referenzen)
     * @author Berger Sandro
     */
    public ArrayList<String> getImagePath() {
        return imagePath;
    }

    /**
     * Die Method liefer alle Audio-Referenzen die sich in der
     * tbl_Sprachaufnahme befinden.
     *
     * @return ArrayList of String (Audio-Referenzen)
     * @author Berger Sandro
     */
    public ArrayList<String> getAudioPath() {
        return audioPath;
    }

    /**
     * Erstellt ein neues HTTPHandler objekt und sendet somit die
     * Daten der Datenbank an das PHP-File.
     * Als nächstes werden die Bild und Audiodareferenzen aus der Datenbank
     * ausgelesen und mittels des FTPHandlers an den FTP-Server gesendet.
     *
     * @author Berger Sandro
     */
    public void send(View v) throws IOException {
        HTTPHandler httpHandler = new HTTPHandler(this);
        loadData();
        try {
            if (!getImagePath().isEmpty()) {
                for (String filepath : getImagePath()) {
                    FTPHandler ftp1 = new FTPHandler(filepath);
                }
            }
            if (!getAudioPath().isEmpty()) {
                for (String filepath : getAudioPath()) {
                    FTPHandler ftp1 = new FTPHandler(filepath);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
        startActivity(intent);


        Context context = getApplicationContext();
        CharSequence text = "Erfolgreich gespeichert";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Diese Methode wird vom Play button ausgerufen. Sie startet das Sprachmemo und die Seekbar
     *
     * @param v ist die angeklickte view
     */
    public void playButtonClick(View v) {
        try {
            Toast.makeText(getApplicationContext(), "Aufnahme wird gestartet",
                    Toast.LENGTH_SHORT).show();
            seek.setVisibility(View.VISIBLE);
            dur.setVisibility(View.VISIBLE);


            myplayer.start();
            finalTime = myplayer.getDuration();
            startTime = myplayer.getCurrentPosition();

            if (oneTimeOnly == 0) {
                seek.setMax((int) finalTime);
                oneTimeOnly = 1;
            }
            pos.setText(String.format("Gesamtdauer: " + "%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
            );

            dur.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );


            seek.setProgress((int) startTime);
            myHandler.postDelayed(UpdateSongTime, 100);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Diese Methode pausiert die Sprachaufnahme.
     *
     * @param v ist die geklickte view
     */
    public void pauseButtonClick(View v) {
        try {
            myplayer.pause();
            Toast.makeText(getApplicationContext(), "Aufnahme ist auf pause",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Diese Methode stoppt die Aufnhame und erstellt zugleich noch einmal einen Mediaplayer damit man ihn wiederholt abspielen kann
     *
     * @param v ist die geklickte view
     */
    public void stopButtonClick(View v) {
        try {

            myplayer.stop();
            myplayer.reset();
            myplayer.release();

            myplayer = new MediaPlayer();
            FileInputStream audioFile = new FileInputStream(audiofpad);
            myplayer.setDataSource(audioFile.getFD());
            myplayer.prepare();

            dur.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) 00),
                    TimeUnit.MILLISECONDS.toSeconds((long) 00) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) 00))));

            Toast.makeText(getApplicationContext(), "Aufnahme ist gestoppt",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}