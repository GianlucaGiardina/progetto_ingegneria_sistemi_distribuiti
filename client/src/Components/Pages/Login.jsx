export const Login = () => {
  return (
    <>
      <div className="bg-lime-400 w-full h-full flex items-center justify-center">
        <div className="bg-white w-[448px] h-80 rounded-lg shadow-2xl flex flex-col items-center justify-center">
          <form className="flex flex-col items-center justify-center space-y-4">
            <input
              type="text"
              placeholder="Username"
              className="w-80 p-2 border border-gray-300 rounded-md mb-4"
            />
            <input
              type="password"
              placeholder="Password"
              className="w-80 p-2 border border-gray-300 rounded-md mb-4"
            />
            <button className="w-80 p-2 bg-lime-600 text-white font-bold rounded-md">
              Login
            </button>
          </form>
          <span className="font-light text-sm mt-4">
            Non sei ancora registrato? <a href="/register">Crea un account</a>
          </span>
        </div>
      </div>
    </>
  );
};
