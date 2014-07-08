JFugue-for-Android
==================

Music library [JFugue](http://www.jfugue.org/) porting for Android<br />
Based on the version 4.0.3.

*Currently, work in progress*

Usage
=====

Initialization / Termination
--------------
At the main Activity or Fragment, append `MidiSystem.initialize(Context)` in `onCreate` and append `MidiSystem.terminate()` in `onDestroy`.

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MidiSystem.initialize(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MidiSystem.terminate();
    }
```

Use of JFugue
-------------
See the [original documents](http://www.jfugue.org/examples.html).

## Playing C scale:
```java
	Player player = new Player();
	player.play("C D E F G A B");
```

Different from the original JFugue, `Player.play(...)` **doesn't block the current thread**.
To wait finishing playing, `player.getSequencer().addMetaEventListener(...)` can be used.

For example:
```java
	player.getSequencer().addMetaEventListener(new MetaEventListener() {
		@Override
		public void meta(MetaMessage metaMessage) {
			if (MetaMessage.TYPE_END_OF_TRACK == metaMessage.getType()) {
				// XXX playing finished
			}
		}
	});
```

## Writing MusicXML to file:
```java
	FileOutputStream file = context.openFileOutput("music.xml", MODE_PRIVATE);

	MusicXmlRenderer renderer = new MusicXmlRenderer();
	MusicStringParser parser = new MusicStringParser();
	parser.addParserListener(renderer);

	Pattern pattern = new Pattern("C D E F G A B |");
	parser.parse(pattern);
	
	Serializer serializer = new Serializer(file, "UTF-8");
	serializer.setIndent(4);
	serializer.write(renderer.getMusicXMLDoc());

	file.flush();
	file.close();
```

License
=======
- JFugue for Android's license is same as the original, [LGPL](https://www.gnu.org/licenses/lgpl.html).
- Other modified referencial library's license is also same as original. See each directories' license file for more information.
