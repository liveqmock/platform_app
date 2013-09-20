package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.adapters.CardDetailsAdapter;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDetailCommentListData;
import com.apalya.myplex.data.CardDetailDataHolder;
import com.apalya.myplex.data.CardDetailDescriptionData;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.CustomFastScrollView;
import com.apalya.myplex.views.FadeInNetworkImageView;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;
import com.apalya.myplex.views.JazzyViewPager;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.apalya.myplex.views.OutlineContainer;

public class CardDetails extends BaseFragment implements
		ItemExpandListenerCallBackListener {
	private ListView listView;
	private CustomFastScrollView fastScrollView;
	private CardDetailsAdapter mAdapter;
	private LayoutInflater mInflater;
	private int mDetailType = Profile;
	public static final int Profile = 0;
	public static final int MovieDetail = 1;
	public static final int TvShowsDetail = 2;
	public static final int LiveTvDetail = 3;
	public View rootView;	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.carddetails, container,false);
		listView = (ListView) rootView.findViewById(android.R.id.list);
		fastScrollView = (CustomFastScrollView) rootView.findViewById(R.id.fast_scroll_view);
		prepareContent();
		return rootView;
	}
	private CardData mCardData;
	private void prepareContent() {
		mAdapter = new CardDetailsAdapter(getContext());
		mAdapter.setItemExpandListener(this);
		if(mDataObject instanceof CardData){
			mCardData = (CardData)mDataObject;
			mMainActivity.setTitle(mCardData.title);
			NetworkImageView imageview = (NetworkImageView)rootView.findViewById(R.id.carddetails_image);
			imageview.setImageUrl(mCardData.imageUrl, MyVolley.getImageLoader());
		}
		
		dummyData();
		fastScrollView.listItemsChanged();
	}

	private CardDetailDataHolder createDescriptionData() {
		CardDetailDataHolder descriptionData = new CardDetailDataHolder();
		descriptionData.mFilterName = "Description";
		descriptionData.mLabel = "Description";
		descriptionData.mShowinQuickLaunch = true;
		CardDetailDescriptionData subData = new CardDetailDescriptionData();
		subData.mContentFullDescription = "Left for dead on a sun-scorched planet, Riddick finds himself up against an alien race of predators. Activating an emergency beacon alerts two ships: one carrying a new breed of mercenary, the other captained by a man from Riddick&apos;s past.";
		subData.mContentBriefDescription = "Left for dead on a sun-scorched planet, Riddick finds ....";
		subData.mRating = (float) 3.5;
		if(mCardData != null){
			subData.mTitle = mCardData.title;
		}else{
			subData.mTitle = "Title";
		}
		descriptionData.mData = subData;
		return descriptionData;
	}

	private CardDetailMediaData createMedia(String url) {
		CardDetailMediaData mediaData = new CardDetailMediaData();
		mediaData.mThumbnailDescription = "ThumbnailDescription";
		mediaData.mThumbnailName = "ThumbnailName";
		mediaData.mThumbnailUrl = url;
		mediaData.mThumbnailMime = "ThumbnailDescription";
		return mediaData;

	}

	private int count = 0;

	private CardDetailDataHolder createImageData() {
		CardDetailDataHolder descriptionData = new CardDetailDataHolder();
		if(count == 0){
			descriptionData.mFilterName = "Audio Release ";
			descriptionData.mLabel = "Audio Release ";
		}else if(count == 1){
			descriptionData.mFilterName = "Muhurtham Photos";
			descriptionData.mLabel = "Muhurtham Photos";
		}else{
			descriptionData.mFilterName = "Related Content";
			descriptionData.mLabel = "Related Content";
		}
		count++;
		descriptionData.mShowinQuickLaunch = true;
		CardDetailMediaListData subData = new CardDetailMediaListData();
		for (int i = 0; i < URLS.length; i++) {
			subData.mList.add(createMedia(URLS[i]));
		}
		descriptionData.mData = subData;
		return descriptionData;
	}

	private CardDetailDataHolder createCommentData(){
		CardDetailDataHolder descriptionData = new CardDetailDataHolder();
		descriptionData.mFilterName = "Commens and Review";
		descriptionData.mLabel = "Commens and Review";
		descriptionData.mShowinQuickLaunch = true;
		CardDetailCommentListData subData = new CardDetailCommentListData();
		descriptionData.mData = subData;
		return descriptionData;
		
	}
	private void dummyData() {
		List<CardDetailDataHolder> datalist = new ArrayList<CardDetailDataHolder>();
		datalist.add(createDescriptionData());
		datalist.add(createImageData());
//		datalist.add(createImageData());
//		datalist.add(createImageData());
		datalist.add(createCommentData());

		mAdapter.setData(datalist);
		listView.setAdapter(mAdapter);
	}

	private static final String[] URLS = {
			"https://lh6.googleusercontent.com/-HEeoO3k3bPg/S0VKWAJUlbI/AAAAAAAAAik/k1x42L8UIvw/Movie-GhostRider-001.jpg",
			"https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg",
			"https://lh3.googleusercontent.com/-yqLKT4RAfBM/S32v0NNVTbI/AAAAAAAAKyw/2ggyry4KiCE/Nature%252520Wallpapers%252520%25252880%252529.jpg",
			"https://lh5.googleusercontent.com/-d-qS8knzDP4/SePRfjfPYhI/AAAAAAAACVA/jxox5vRCphw/IMG_0084.jpg",
			"https://lh5.googleusercontent.com/-eurfd_3DDJM/SYpR7j0o8CI/AAAAAAAAJ8k/XRRlN8bdQlA/DSCF3739r.jpg",
			"https://lh5.googleusercontent.com/-K0Weq3ovQ2Y/SPrz1dedUWI/AAAAAAAAARc/1-fuKwsJPHs/IMG_4450.JPG",
			"https://lh3.googleusercontent.com/-RgQYezPeGck/SMC0KWFderI/AAAAAAAAE-w/Tm0JFwb-1Yc/100_5136.jpg",
			"https://lh3.googleusercontent.com/-sjtaMlX_2Qo/Sen1maYoUeI/AAAAAAAABds/8ABF3laHiqg/CA-wp6.jpg",
			"https://lh6.googleusercontent.com/-V4-QM6drP5c/SA66lIlP2cI/AAAAAAAAAIA/SjF8lVpf5hI/Denali-Dance-Framed.jpg",
	// "http://lh5.ggpht.com/_mrb7w4gF8Ds/TCpetKSqM1I/AAAAAAAAD2c/Qef6Gsqf12Y/s144-c/_DSC4374%20copy.jpg",
	// "http://lh5.ggpht.com/_Z6tbBnE-swM/TB0CryLkiLI/AAAAAAAAVSo/n6B78hsDUz4/s144-c/_DSC3454.jpg",
	// "http://lh3.ggpht.com/_GEnSvSHk4iE/TDSfmyCfn0I/AAAAAAAAF8Y/cqmhEoxbwys/s144-c/_MG_3675.jpg",
	// "http://lh6.ggpht.com/_Nsxc889y6hY/TBp7jfx-cgI/AAAAAAAAHAg/Rr7jX44r2Gc/s144-c/IMGP9775a.jpg",
	// "http://lh3.ggpht.com/_lLj6go_T1CQ/TCD8PW09KBI/AAAAAAAAQdc/AqmOJ7eg5ig/s144-c/Juvenile%20Gannet%20despute.jpg",
	// "http://lh6.ggpht.com/_ZN5zQnkI67I/TCFFZaJHDnI/AAAAAAAABVk/YoUbDQHJRdo/s144-c/P9250508.JPG",
	// "http://lh4.ggpht.com/_XjNwVI0kmW8/TCOwNtzGheI/AAAAAAAAC84/SxFJhG7Scgo/s144-c/0014.jpg",
	// "http://lh6.ggpht.com/_lnDTHoDrJ_Y/TBvKsJ9qHtI/AAAAAAAAG6g/Zll2zGvrm9c/s144-c/000007.JPG",
	// "http://lh6.ggpht.com/_qvCl2efjxy0/TCIVI-TkuGI/AAAAAAAAOUY/vbk9MURsv48/s144-c/DSC_0844.JPG",
	// "http://lh4.ggpht.com/_TPlturzdSE8/TBv4ugH60PI/AAAAAAAAMsI/p2pqG85Ghhs/s144-c/_MG_3963.jpg",
	// "http://lh4.ggpht.com/_4f1e_yo-zMQ/TCe5h9yN-TI/AAAAAAAAXqs/8X2fIjtKjmw/s144-c/IMG_1786.JPG",
	// "http://lh6.ggpht.com/_iFt5VZDjxkY/TB9rQyWnJ4I/AAAAAAAADpU/lP2iStizJz0/s144-c/DSCF1014.JPG",
	};

	@Override
	public void OnItemExpand(View v) {
		if (v == null) {
			return;
		}
		if (v.getTag() instanceof CardDetailMediaListData) {
			CardDetailMediaListData mainData = (CardDetailMediaListData) v
					.getTag();
			mMediaList = mainData.mList;
			showAlbumDialog();
		}
	}

	private void showAlbumDialog() {
		mAlbumDialog = new CustomDialog(getContext());
		mAlbumDialog.setContentView(R.layout.albumview);
		setupJazziness(TransitionEffect.Stack);
		mAlbumDialog.setCancelable(true);
		mAlbumDialog.show();
	}

	private List<CardDetailMediaData> mMediaList = null;
	private JazzyViewPager mJazzy;
	private CustomDialog mAlbumDialog;

	private void setupJazziness(TransitionEffect effect) {
		mJazzy = (JazzyViewPager) mAlbumDialog.findViewById(R.id.jazzy_pager);
		mJazzy.setTransitionEffect(effect);
		mJazzy.setAdapter(new MainAdapter());
		// mJazzy.setPageMargin(30);
	}

	private class MainAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {

			View v = mInflater.inflate(R.layout.albumitem, null);

			FadeInNetworkImageView imageView = (FadeInNetworkImageView) v
					.findViewById(R.id.albumitem_imageView1);
//			Random rnd = new Random();
			// int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(128),
			// rnd.nextInt(64));
			// imageView.setBackgroundColor(color);
//			TextView text = (TextView) v.findViewById(R.id.albumitem_textView1);
//			text.setText("" + position);
			imageView.setImageUrl(mMediaList.get(position).mThumbnailUrl,
					MyVolley.getImageLoader());
			container.addView(v);
			mJazzy.setObjectForPosition(v, position);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(mJazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			return mMediaList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			if (view instanceof OutlineContainer) {
				return ((OutlineContainer) view).getChildAt(0) == obj;
			} else {
				return view == obj;
			}
		}
	}
}
