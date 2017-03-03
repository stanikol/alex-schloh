val uri ="" //"postgres://uzijzfaeepoouj:e6d3133d0b7dc034e5d7ceb20305bef1220562f5b3da13c9ea73a15934979549@ec2-54-225-107-107.compute-1.amazonaws.com:5432/d77d71plaq7bhq"""
val Regex = """postgres://(\w+):(\w+)@(.*):(\d+)/(.*)""".r

uri match {
  case Regex(usr, pwd, url, port, db) =>
    (usr, pwd, url, port, db)
  case _ => 0
}
