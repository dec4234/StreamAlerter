package net.dec4234.streamAlerter.youtube;

import net.dec4234.streamAlerter.framework.YoutubeStreamEvent;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class YoutubeStreamCheck {

	private static Timer timer = new Timer();

	private YoutubeStreamEvent youtubeStreamEvent;
	private String channelID;
	private int frequency;
	private String lastLink;

	private TimerTask timerTask;

	public YoutubeStreamCheck(YoutubeStreamEvent youtubeStreamEvent, String channelID, int frequency) {
		this.youtubeStreamEvent = youtubeStreamEvent;
		this.channelID = channelID;
		this.frequency = frequency;
	}

	public void beginWatching() {
		cancel();

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if(isStreaming()) {
					youtubeStreamEvent.onStream(getLastLink());
				}
			}
		};

		timer.schedule(timerTask, 0, 1000L * frequency);
	}

	public void cancel() {
		if(timerTask != null) {
			timerTask.cancel();
		}
	}

	public String getCanonicalLink() {
		String liveUrl = "https://www.youtube.com/channel/" + channelID + "/live";

		Connection connection = Jsoup.connect(liveUrl).header("Connection", "close");
		Document document = null;
		try {
			document = connection.get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(document != null) {
			for (Element element : document.getElementsByTag("link")) {
				if (element.hasAttr("rel")) {
					String key = element.attr("rel");

					if (key.equals("canonical")) {
						String link = element.attr("href");
						lastLink = link;

						return link;
					}
				}
			}
		}

		return null;
	}

	public String getLastLink() {
		return lastLink;
	}

	private int count = 0;

	public boolean isStreaming() {
		String link = getCanonicalLink();

		if(link == null) {
			return false;
		}

		return !link.contains(channelID);
	}
}
