package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import crystal.tech.gimmecollage.lenta_api.LentaAPI;
import crystal.tech.gimmecollage.lenta_api.Storage;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        if (!LentaAPI.initialized()) {
            LentaAPI.init(getActivity());
        }

        final ListView newsFeed = (ListView) rootView.findViewById(R.id.listView);

        final LinearLayout linlaHeaderProgress = (LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress);
        // SHOW THE SPINNER WHILE LOADING FEEDS
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        LentaAPI.with(new LentaAPI.Listener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Posts successfully loaded!",
                        Toast.LENGTH_SHORT).show();
//                reloadPosts(rootView);
                newsFeed.setAdapter(new NewsfeedAdapter(getActivity()));

                linlaHeaderProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(), "Error: Can't load posts.",
                        Toast.LENGTH_SHORT).show();

                linlaHeaderProgress.setVisibility(View.GONE);
            }
        }).updatePosts();

        return rootView;
    }

    private void updateImageView(ImageView iv, final View pb, Storage.ImageInfo imageInfo) {
        if (pb != null)
            pb.setVisibility(View.VISIBLE);
        Picasso.with(getActivity());

        Callback on_load = new Callback() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Image from post loaded successfully!");
                if (pb != null)
                    pb.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Log.e(TAG, "Image from post not loaded!");
                if (pb != null)
                    pb.setVisibility(View.GONE);
            }
        };

        Picasso.with(getActivity())
                .load(imageInfo.url)
                .into(iv, on_load);
    }

    private void reloadPosts(View rootView) {
        ArrayList<Storage.PostInfo> posts = LentaAPI.getPosts();
        LinearLayout llMain = (LinearLayout)rootView.findViewById(R.id.linearLayout);

        for (int i = 0; i < posts.size(); i++) {
            Storage.ImageInfo imageInfo = posts.get(i).image_preview.url.isEmpty() ?
                    posts.get(i).image_preview : posts.get(i).image;

            if (i > llMain.getChildCount() - 1) {
                LinearLayout ll = (LinearLayout) getActivity().getLayoutInflater().inflate(
                        R.layout.layout_lenta_post, null);
                ImageView iv = (ImageView) ll.findViewById(R.id.imageView);
                iv.setPadding(0, 0, 0, 0);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                RelativeLayout imageRL = (RelativeLayout)ll.findViewById(R.id.imageRelativeLayout);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) imageRL.getLayoutParams();
                layoutParams.width = imageInfo.width;
                layoutParams.height = imageInfo.height;
                imageRL.setLayoutParams(layoutParams);
                imageRL.setPadding(0, 0, 0, 0);

                LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llLayoutParams.setMargins(24, 24, 24, 24);
                llMain.addView(ll, llLayoutParams);
            }

            LinearLayout ll = (LinearLayout) llMain.getChildAt(i);
            ImageView iv = (ImageView) ll.findViewById(R.id.imageView);
            ProgressBar pb = (ProgressBar) ll.findViewById(R.id.progressBar);
            if (!imageInfo.url.isEmpty())
                updateImageView(iv, pb, imageInfo);

            RelativeLayout imageRL = (RelativeLayout)ll.findViewById(R.id.imageRelativeLayout);
            imageRL.setVisibility(imageInfo.url.isEmpty() ? View.GONE : View.VISIBLE);

            TextView txtNickname = (TextView) ll.findViewById(R.id.nicknameTextView);
            txtNickname.setText("#" + posts.get(i).nickname);
            txtNickname.setTextColor(getResources().getColor(R.color.design_blue));

            TextView txtPost = (TextView) ll.findViewById(R.id.postTextView);
            txtPost.setText(posts.get(i).text);
        }
    }

    public class NewsfeedAdapter extends BaseAdapter {
        private Context mContext;

        public NewsfeedAdapter(Context context) { mContext = context; }

        public int getCount() {
            return LentaAPI.getPosts().size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ArrayList<Storage.PostInfo> posts = LentaAPI.getPosts();
            Storage.ImageInfo imageInfo = posts.get(position).image_preview.url.isEmpty() ?
                    posts.get(position).image_preview : posts.get(position).image;

            if (view == null) {  // if it's not recycled, initialize some attributes
                view = getActivity().getLayoutInflater().inflate(R.layout.layout_lenta_post,
                        parent, false);

                view.setTag(R.id.imageView, view.findViewById(R.id.imageView));
                view.setTag(R.id.imageRelativeLayout,
                        view.findViewById(R.id.imageRelativeLayout));
                view.setTag(R.id.progressBar, view.findViewById(R.id.progressBar));
                view.setTag(R.id.nicknameTextView, view.findViewById(R.id.nicknameTextView));
                view.setTag(R.id.postTextView, view.findViewById(R.id.postTextView));

                ImageView iv = (ImageView) view.getTag(R.id.imageView);
                iv.setPadding(0, 0, 0, 0);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                RelativeLayout imageRL = (RelativeLayout)view.getTag(R.id.imageRelativeLayout);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) imageRL.getLayoutParams();
                layoutParams.width = imageInfo.width;
                layoutParams.height = imageInfo.height;
                imageRL.setLayoutParams(layoutParams);
                imageRL.setPadding(0, 0, 0, 0);

                LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llLayoutParams.setMargins(24, 24, 24, 24);

                //Log.d(DEBUG_TAG, "View constructed, " + position);
            }


            ImageView iv = (ImageView) view.getTag(R.id.imageView);
            ProgressBar pb = (ProgressBar) view.getTag(R.id.progressBar);
            if (!imageInfo.url.isEmpty())
                updateImageView(iv, pb, imageInfo);

            RelativeLayout imageRL = (RelativeLayout)view.getTag(R.id.imageRelativeLayout);
            imageRL.setVisibility(imageInfo.url.isEmpty() ? View.GONE : View.VISIBLE);

            TextView txtNickname = (TextView) view.getTag(R.id.nicknameTextView);
            txtNickname.setText("#" + posts.get(position).nickname);
            txtNickname.setTextColor(getResources().getColor(R.color.design_blue));

            TextView txtPost = (TextView) view.getTag(R.id.postTextView);
            txtPost.setText(posts.get(position).text);

            //Log.d(DEBUG_TAG, "View updated, " + position);

            return view;
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
