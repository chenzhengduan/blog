请为{subject} {grade}年级生成一份试卷。
满分：{totalScore}分，难度：{difficulty}{topic}{extra}
{distribution}

先使用 chuti skill 加载知识范围文件，再调用 submit_outline 提交大纲，然后逐题调用 submit_question 提交每道题。
