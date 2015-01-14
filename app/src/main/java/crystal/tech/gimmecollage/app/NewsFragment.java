package crystal.tech.gimmecollage.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

        LentaAPI.with(new LentaAPI.Listener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Posts successfully loaded!",
                        Toast.LENGTH_SHORT).show();
                reloadPosts(rootView);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(), "Error: Can't load posts.",
                        Toast.LENGTH_SHORT).show();
            }
        }).updatePosts();

        return rootView;
    }

    private void updateImageView(ImageView iv, final View pb, Storage.PostInfo postInfo) {
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

        String path = !postInfo.image_preview.isEmpty() ?
                postInfo.image_preview : postInfo.image;
        if (!path.isEmpty()) {
            Picasso.with(getActivity())
                    .load(path)
                    .into(iv, on_load);
        }
    }

    private void reloadPosts(View rootView) {
        ArrayList<Storage.PostInfo> posts = LentaAPI.getPosts();
        LinearLayout llMain = (LinearLayout)rootView.findViewById(R.id.linearLayout);
        for (int i = 0; i < posts.size(); i++) {
            if (i > llMain.getChildCount() - 1) {
                LinearLayout ll = (LinearLayout) getActivity().getLayoutInflater().inflate(
                        R.layout.layout_lenta_post, null);
                ImageView iv = (ImageView) ll.findViewById(R.id.imageView);
                iv.setPadding(0, 0, 0, 0);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                llMain.addView(ll);
            }
            LinearLayout ll = (LinearLayout) llMain.getChildAt(i);
            ImageView iv = (ImageView) ll.findViewById(R.id.imageView);
            updateImageView(iv, null, posts.get(i));
            TextView txNickname = (TextView) ll.findViewById(R.id.nicknameTextView);
            txNickname.setText(posts.get(i).nickname);
            TextView txPost = (TextView) ll.findViewById(R.id.postTextView);
            txPost.setText(posts.get(i).text);
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
