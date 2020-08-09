package jp.techacademy.takimoto.kento.qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.View
import android.widget.ListView
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }



    private val mFavoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {//お気に入り登録済みの場合呼び出しあり

            Log.d("loglog", "onChildAdded")

            //フラグtrue
            flag = true

            //ボタンの色変える処理
            if(flag == true){
                button1.text="お気に入り済み"
                button1.setBackgroundColor(Color.rgb(255,255,0))
                Log.d("loglog", "今flag_trueです")
            }else{
                button1.text="お気に入り追加"
                button1.setBackgroundColor(Color.rgb(164,164,164))
                Log.d("loglog", "今flag_falseです")
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    //フラグ初期化false
    var flag: Boolean = false



    override fun onResume(){
        super.onResume()
        //復帰時最初に呼ばれる
        //ログインの確認 & お気に入りの確認など

        Log.d("loglog", "onResume")

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // 未ログイン
            button1.visibility = View.INVISIBLE
        }else{
            //-------------お気に入りボタン処理------------

            button1.visibility = View.VISIBLE

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = dataBaseReference.child(FavoritesPATH).child(user!!.uid)
                .child(mQuestion.questionUid)
            favoriteRef.addChildEventListener(mFavoriteListener)
            button1.setOnClickListener {
                if(flag == true){
                    favoriteRef.setValue(null)
                    Log.d("loglog", "onResume2")
                    button1.text="お気に入り追加する"
                    button1.setBackgroundColor(Color.rgb(164,164,164))
                    flag = false
                    Log.d("loglog", flag.toString())
                }else{
                    val data = HashMap<String, String>()
                    data["genre"] = mQuestion.genre.toString()
                    data["questionUid"] = mQuestion.questionUid.toString()
                    favoriteRef.setValue(data)
                    button1.text="お気に入り追加済"
                    button1.setBackgroundColor(Color.rgb(255,255,0))
                    flag = true
                    Log.d("loglog", flag.toString())

                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)


        Log.d("loglog", "onCreate")

        //----------お気に入り追加済みか確認----------

//        val qid =
//
//        if(qid == null){
//            var Flag = "True"
//        }else{
//            var Flag = "False"
//        }
//
        //------------------------------------------

//        val user = FirebaseAuth.getInstance().currentUser
//
//        if (user == null) {
//            // 未ログイン
//            button1.visibility = View.INVISIBLE
//        }
//        //-------------お気に入りボタン処理------------
//
//        button1.setOnClickListener {
//            val dataBaseReference = FirebaseDatabase.getInstance().reference
//            val favoriteRef = dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(mQuestion.questionUid)
//            favoriteRef.addChildEventListener(mFavoriteListener)
//            val data=HashMap<String, String>()
//            data["genre"]=mQuestion.genre.toString()
//            favoriteRef.setValue(data)
//
//            //---------------------------------------
//        }

        // 渡ってきたQuestionオブジェクト保持
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}